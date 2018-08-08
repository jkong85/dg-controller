package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BackupServiceMaintainThread implements Runnable{
    private static final Logger logger = LogManager.getLogger(ControllerCoreApplication.class);
    private Thread t;

    public BackupServiceMaintainThread() {
    }

    public void run() {
        logger.info("Running backup service maintain thread");
        ApiServerCmd apiServerCmd = new ApiServerCmd();
        while(true) {
            // synchronize
            BackupServiceRequest request = ControllerCoreApplication.bkServiceRequestQueue.poll();
            if(request != null){
                Integer index = ControllerCoreApplication.bkServiceIndexPoolStack.pop();
                String node = request.node;
                String type = request.type;
                Integer port_eureka = ControllerCoreApplication.nodePortsPool.pop();
                Integer port_zuul = port_eureka + 1;
                BackupService backupService = apiServerCmd.createBackupService(request, index, port_eureka, port_zuul);
                if(backupService != null){
                    ControllerCoreApplication.bkServiceNotReadyPoolMap.get(node).get(type).push(backupService);
                }else{
                    // restore all things
                    ControllerCoreApplication.bkServiceIndexPoolStack.push(index);
                    ControllerCoreApplication.bkServiceRequestQueue.offer(request);
                }
            }
            try {
                Thread.sleep(1000);
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
