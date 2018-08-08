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
    private Thread t;

    public BkServiceCheckDeployReadyThread() {
    }
    // Keep that there are at least BACK_LIMIT available BackupService for each type on each node
    public void run() {
        logger.info("Running BkServiceCheckAvailNumberThread to guarantee that a certain number of BackupServices are available!");
        while(true) {
            for(String node : ControllerCoreApplication.NODE_LIST){
                for(String type : ControllerCoreApplication.IMO_TYPE){
                    try { Thread.sleep(500);
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
        // create a test K8S service
        String k8sServiceName = backupService.selector;
        //TODO: lock it???
        Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer node_port_zuul = node_port_eureka + 1;
        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(k8sServiceName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());
        String nodeIP = ControllerCoreApplication.nodeIPMap.get(backupService.node);
        String[] urlList = new String[backupService.deploymentsList.size()];
        String ipPrefix = "http://" + nodeIP + ":" + node_port_zuul;
        String ipPostfix = "/ready";
        for(int i=0; i<urlList.length; i++){
            urlList[i] = ipPrefix + backupService.deploymentsList.get(i).name + ipPostfix;
        }
        int cnt = 2;
        while(cnt-->0){
            if(isAllDeploymentReady(urlList)){
                logger.trace("delete the k8sService used by ready checking: " + k8sServiceName);
                apiServerCmd.deleteService(k8sServiceName, node_port_eureka);
                return true;
            }
        }
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
                    logger.trace("Not ready of deployments of " + url);
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