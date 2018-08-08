package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestClientException;

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
        logger.info("Running BkServiceCheckAvailNumberThread to guarantee that a certain number of BackupServices are available!");
        while(true) {
            for(String node : ControllerCoreApplication.NODE_LIST){
                for(String type : ControllerCoreApplication.IMO_TYPE){
                    try { Thread.sleep(5000);
                    } catch (InterruptedException ie) { }

                    logger.debug("check current bkservice for node : " + node + ", type : " + type);
                    String nodetype = node + "+" + type;
                    if(ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).isEmpty()) {
                        continue;
                    }
                    //The new not ready bkservice is put at the end of list, so here we check the head of the list firstly
                    BackupService backupService = ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).get(0);
                    if(isReady(backupService)){
                        logger.debug("The new BackupService : " + backupService.toString() + " is ready now!");
                        logger.debug("Before moving the new bkService,  bkServiceNotReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).toString());
                        logger.debug("Before moving the new bkService,  bkServiceReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).toString());
                        ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).remove(0);
                        backupService.status = ControllerCoreApplication.BK_SERVICE_STATUS_AVAILABLE;
                        ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).add(backupService);
                        logger.debug("After moving the new bkService,  bkServiceNotReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).toString());
                        logger.debug("After moving the new bkService,  bkServiceReadyPoolMap of " + nodetype + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).toString());
                    }else{
                        logger.debug("The new BackupService : " + backupService.toString() + " is not ready yet!");
                    }
                }
            }
        }
    }
    private boolean isReady(BackupService backupService){
        String k8sServiceName = "it-is-a-test-service-to-check-deployment-ready-make-it-unique";
        //TODO: lock it???
        //Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        //Integer node_port_zuul = node_port_eureka + 1;
        Integer node_port_eureka = PORT_EUREKA_CHECK_SERVICE;
        Integer node_port_zuul = PORT_ZUUL_CHECK_SERVICE;

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(k8sServiceName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());
        String nodeIP = ControllerCoreApplication.nodeIPMap.get(backupService.node);
        String[] urlList = new String[backupService.deploymentsList.size()];
        String ipPrefix = "http://" + nodeIP + ":" + node_port_zuul;
        String ipPostfix = "/ready";
        for(int i=0; i<urlList.length; i++){
            urlList[i] = ipPrefix + backupService.deploymentsList.get(i).name + ipPostfix;
        }

        if(isAllDeploymentReady(urlList)){
            logger.info("The backup service is ready, delete the k8sService test service : " + k8sServiceName);
            apiServerCmd.deleteService(k8sServiceName, node_port_eureka);
            return true;
        }
        apiServerCmd.deleteService(k8sServiceName, node_port_eureka);
        return false;
    }

    private static boolean isAllDeploymentReady(String[] urlList){
        for(String url : urlList){
            boolean flag = false;
            int i = 5;
            while(i-- > 0){
                try {
                    Http.httpGet(url);
                    flag = true;
                    break;
                } catch (RestClientException re) {
                    logger.info("Not ready of deployments of " + url);
                }
            }
            if(!flag){
                return false;
            }
        }
        return true;
    }

    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }
}
