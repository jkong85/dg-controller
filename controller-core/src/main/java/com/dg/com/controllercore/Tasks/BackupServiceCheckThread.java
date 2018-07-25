package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;

public class BackupServiceCheckThread implements Runnable{
    private Thread t;

    public BackupServiceCheckThread() {

    }

    public void run() {
        System.out.println("Running backup service maintain thread ");
        while(true) {
            //public static Map<String, Map<String, Stack<BackupService>>> bkServicePoolMap;
            for(String node : ControllerCoreApplication.NODE_LIST){
                for(String type : ControllerCoreApplication.IMO_TYPE){
                    if(ControllerCoreApplication.bkServicePoolMap.get(node).get(type).size() < ControllerCoreApplication.BACKUP_LIMIT){
                        ControllerCoreApplication.bkServiceRequestQueue.offer(new BackupServiceRequest(node, type));
                    }
                }
            }

            try {
                Thread.sleep(30000);
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
