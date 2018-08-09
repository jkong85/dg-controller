package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by jkong on 8/9/18.
 */
public class DgCmds {
    private static final Logger logger = LogManager.getLogger(DgCmds.class);
    public DgCmds(){ }

    // find backup service, and bind them
    public static DG createDGQuick(String dgName, IMO imo, String type, String node){
        String nodeType = node + "+" + type;
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).isEmpty()){
            logger.warn("No available DG on core cloud, just wait!");
            return null;
        }
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).isEmpty()){
            logger.warn("No available DG on core cloud, just wait!");
            return null;
        }
        BackupService backupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).get(0);
        backupService.status = ControllerCoreApplication.BK_SERVICE_STATUS_USED;
        logger.debug("Find the BackupService on node : " + node + " for IMO request: " + dgName + " => " + backupService.toString());

        Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer node_port_zuul = node_port_eureka + 1;
        logger.debug("Allocate ports: " + node_port_eureka + ", " + node_port_zuul + " to " + dgName);

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(dgName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());

        String coreIP = ControllerCoreApplication.nodeIpMap.get(node);

        DG dg = new DG(dgName, type, node, coreIP, node_port_zuul.toString(), backupService);

        imo.dgList.add(dg);
        ControllerCoreApplication.IMOMap.put(dgName, imo);
        logger.info("New DG is allocated for " + dgName+ " on node: " + node+ " => " + dg.toString());

        return dg;
    }

    public static DG createDGSlow(String dgName, IMO imo, String type, String node){
        return null;
    }
}
