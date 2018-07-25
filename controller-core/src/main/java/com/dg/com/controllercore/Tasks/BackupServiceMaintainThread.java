package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;

public class BackupServiceMaintainThread implements Runnable{
    private Thread t;

    public BackupServiceMaintainThread() {

    }

    public void run() {
        System.out.println("Running backup service maintain thread ");
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
                    ControllerCoreApplication.bkServicePoolMap.get(node).get(type).push(backupService);
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
