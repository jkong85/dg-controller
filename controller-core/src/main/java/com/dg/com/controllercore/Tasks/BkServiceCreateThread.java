package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BkServiceCreateThread implements Runnable{
    private static final Logger logger = LogManager.getLogger(BkServiceCreateThread.class);
    private Thread t;

    public BkServiceCreateThread() {
    }

    public void run() {
        logger.info("Running BkServiceCreateThread to create all deployments for a new BackupService ");
        ApiServerCmd apiServerCmd = new ApiServerCmd();
        while(true) {
            // synchronize
            BackupServiceRequest request = ControllerCoreApplication.bkServiceRequestQueue.poll();
            if(request != null){
                Integer index = ControllerCoreApplication.bkServiceIndexPoolStack.pop();
                String node = request.node;
                String type = request.type;
                Integer port_eureka = ControllerCoreApplication.nodePortsPool.pop();
                //e.g. eureak_port is allocted to 30016, then zuul_port is 30017
                Integer port_zuul = port_eureka + 1;
                BackupService backupService = apiServerCmd.createBackupService(request, index, port_eureka, port_zuul);
                if(backupService != null){
                    ControllerCoreApplication.bkServiceNotReadyPoolMap.get(node+"+"+type).add(backupService);
                }else{
                    logger.warn("New backupservice is not created successfully, rollback by releasing port number and putting it back to bkServiceRequestQueue");
                    logger.info("Before rollback, bkServiceIndexPoolStack is: " + ControllerCoreApplication.bkServiceIndexPoolStack.toString());
                    logger.info("Before rollback, bkServiceRequestQueue is: " + ControllerCoreApplication.bkServiceRequestQueue.toString());
                    // restore all things
                    ControllerCoreApplication.bkServiceIndexPoolStack.push(index);
                    ControllerCoreApplication.bkServiceRequestQueue.offer(request);
                    logger.info("After rollback, bkServiceIndexPoolStack is: " + ControllerCoreApplication.bkServiceIndexPoolStack.toString());
                    logger.info("After rollback, bkServiceRequestQueue is: " + ControllerCoreApplication.bkServiceRequestQueue.toString());
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }
}
