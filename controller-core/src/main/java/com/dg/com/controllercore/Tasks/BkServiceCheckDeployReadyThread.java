package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.kj.dgcommons.DgCommonsApplication;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.Set;

//Check whether the new created BkService is ready
// By send API request to each deployment's API : /ready
public class BkServiceCheckDeployReadyThread implements Runnable{
    private static final Logger logger = LogManager.getLogger(BkServiceCheckDeployReadyThread.class);

    private static final Integer PORT_EUREKA_CHECK_SERVICE = 30003;
    private static final Integer PORT_ZUUL_CHECK_SERVICE = 30004;
    private Thread t;

    public BkServiceCheckDeployReadyThread() {
    }
    // Keep that there are at least BACK_LIMIT available BackupService for each type on each node
    public void run() {
        DgCommonsApplication.delay(60);
        logger.info("Running BkServiceCheckAvailNumberThread to guarantee that a certain number of BackupServices are available!");
        while(true) {
//            printCurBkPool();
            for(String node : ControllerCoreApplication.NODE_LIST){
                for(String type : ControllerCoreApplication.IMO_TYPE){
                    DgCommonsApplication.delay(0);
                    String nodetype = node + "+" + type;
                    if(ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).isEmpty()) {
                        continue;
                    }
                    logger.debug("check current bkservice for node : " + node + ", type : " + type);
                    //The new not ready bkservice is put at the end of list, so here we check the head of the list firstly
                    BackupService backupService = ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).get(0);
                    if(isReady(backupService)){
                        logger.debug("New BackupService is READY ! => " + backupService.toString());
                        logger.debug("Before moving the new bkService,  bkServiceNotReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).toString());
                        logger.debug("Before moving the new bkService,  bkServiceReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).toString());
                        //TODO: lock here?
                        ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).remove(0);
                        ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).add(backupService);
                        logger.debug("After moving the new bkService,  bkServiceNotReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).toString());
                        logger.debug("After moving the new bkService,  bkServiceReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).toString());
                    }else{
                        logger.debug("The new BackupService : " + backupService.toString() + " is not ready yet!");
                        //TODO: lock here?
                        //move it to the end
                        BackupService tmpBkService = ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).remove(0);
                        ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).add(tmpBkService);
                    }
                }
            }
        }
    }
    private boolean isReady(BackupService backupService){
        logger.debug("Check the BackupService ready: " + backupService.toString());
        String k8sServiceName = "ready-test-service";
        //Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        //Integer node_port_zuul = node_port_eureka + 1;
        Integer node_port_eureka = PORT_EUREKA_CHECK_SERVICE;
        Integer node_port_zuul = PORT_ZUUL_CHECK_SERVICE;
        ApiServerCmd apiServerCmd = new ApiServerCmd();
        try {
            logger.debug("Before ready-check, delete the test service if existed!");
            // No need to put the port back
            apiServerCmd.deleteService(k8sServiceName, node_port_eureka, false);
        }catch (HttpClientErrorException e){
            logger.debug("Test service is not existed, ignore it!");
        }
        apiServerCmd.CreateService(k8sServiceName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());
        // wait some time
        //try { Thread.sleep(10000); } catch (InterruptedException ie) { }
        DgCommonsApplication.delay(5);

        String nodeIP = ControllerCoreApplication.nodeIpMap.get(backupService.node);
        logger.info("curr node is: " + backupService.node + " it is ip is: " + nodeIP );

        String[] urlList = new String[backupService.deploymentsList.size()];
        String ipPrefix = "http://" + nodeIP + ":" + node_port_zuul + "/";
        String ipPostfix = "/ready";
        Set<String> basicDeploySet = new HashSet<>();
        basicDeploySet.add("eureka");
        basicDeploySet.add("zuul");
        basicDeploySet.add("mongo");
        for(int i=0; i<urlList.length; i++){
            String curDeploy = backupService.deploymentsList.get(i).serviceType;
            if(!basicDeploySet.contains(curDeploy)) {
                urlList[i] = ipPrefix + curDeploy + ipPostfix;
            }else{
                urlList[i] = null;
            }
        }
        if(isAllDeploymentReady(urlList)){
            logger.info("BackupService: " + backupService.name + " is ready!");
            logger.debug("Delete the k8sService test service : " + k8sServiceName);
            apiServerCmd.deleteService(k8sServiceName, node_port_eureka, false);
            return true;
        }
        logger.info("BackupService: " + backupService.name + " is NOT ready!");
        logger.debug("Delete the k8sService test service : " + k8sServiceName);
        apiServerCmd.deleteService(k8sServiceName, node_port_eureka, false);
        //try { Thread.sleep(5000); } catch (InterruptedException ie) { }
        DgCommonsApplication.delay(5);
        return false;
    }

    private static boolean isAllDeploymentReady(String[] urlList){
        for(int m=0; m<urlList.length; m++){
            String url = urlList[m];
            if(url != null && url.length() > 1) {
                logger.debug("curr URL of ready checking is : " + url);
                boolean flag = false;
                int i = 5;
                while (i-- > 0) {
                    try {
                        Http.httpGet(url);
                        logger.debug("Deployment is ready of URL: " + url);
                        flag = true;
                        break;
                    } catch (RestClientException re) {
                        logger.debug("Deployment is Not ready of URL:  " + url);
                    }
                }
                if (!flag) {
                    return false;
                }
            }else{
                logger.debug("cur URL of ready checking is Null, continue");
            }
        }
        logger.info("All deployments of one Backupservice is ready! ");
        return true;
    }


    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }
}
