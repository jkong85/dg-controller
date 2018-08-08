package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// If the backup available DG is less than the required number, create a new one
// Put the new requst to bkServiceRequestQueue, BackupServerMaintainThread will create the new DG
public class BkServiceCheckAvailNumberThread implements Runnable{
    private static final Logger logger = LogManager.getLogger(BkServiceCheckAvailNumberThread.class);
    private Thread t;

    public BkServiceCheckAvailNumberThread() {

    }

    // Keep that there are at least BACK_LIMIT available BackupService for each type on each node
    public void run() {
        logger.info("Running BkServiceCheckAvailNumberThread to keep there are a certain number of available BackupService");
        while(true) {
            //public static Map<String, Map<String, Stack<BackupService>>> bkServicePoolMap;
            for(String node : ControllerCoreApplication.NODE_LIST){
                for(String type : ControllerCoreApplication.IMO_TYPE){
                    logger.debug("check current bkservice for node : " + node + ", type : " + type);
                    String nodetype = node + "+" + type;
                    int notReadyNumber = ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).size();
                    int readyNumber = ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).size();
                    if( notReadyNumber + readyNumber < ControllerCoreApplication.BACKUP_LIMIT){
                        ControllerCoreApplication.bkServiceRequestQueue.offer(new BackupServiceRequest(node, type));
                    }
                }
            }

            try {
                Thread.sleep(20000);
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
