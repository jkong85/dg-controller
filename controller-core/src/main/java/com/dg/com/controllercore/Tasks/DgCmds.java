package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

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
            logger.warn("No available DG on node" + node + " , just wait!");
            return null;
        }
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).isEmpty()){
            logger.warn("No available DG on node" + node + " , just wait!");
            return null;
        }
        logger.trace("Before allocating, bkServiceReadyPoolMap of " + nodeType + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).toString());
        BackupService backupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).get(0);
        backupService.dgName = dgName;
        backupService.imoName = imo.name;
        ControllerCoreApplication.bkServiceNameMap.put(backupService.name, backupService);
        logger.debug("Find BackupService on node : " + node + " for IMO request: " + dgName + " => " + backupService.toString());

        Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer node_port_zuul = node_port_eureka + 1;
        logger.debug("Allocate ports: " + node_port_eureka + ", " + node_port_zuul + " to " + dgName);

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(dgName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());

        String coreIP = ControllerCoreApplication.nodeIpMap.get(node);
        DG dg = new DG(dgName, type, node, coreIP, node_port_zuul.toString(), backupService);

        //move it out Ready Pool
        ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).remove(0);
        imo.dgList.add(dg);

        logger.trace("After allocating, bkServiceReadyPoolMap of " + nodeType + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).toString());
        logger.info("New DG is allocated for " + dgName+ " on node: " + node+ " => " + dg.toString());

        return dg;
    }

    // The first solution: NO backupservice is available, create all things, and wait
    //TODO: add the logic
    public static DG createDGSlow(String dgName, IMO imo, String type, String node){
        return null;
    }

    // The following steps:
    //  1. delete service.
    //  2, put port_eureka(zuul) to PortPool.
    //  3, Clean MongoDB data
    //  4. Reset internal data structure value in each Microservices of the DG
    //  5. put Backupservice to ReadyPool.
    //  6. remove dg from IMO dglist
    public static boolean releaseDG(IMO imo, DG dg, Boolean flag){
        if(dg.bkService == null){
            logger.warn("Bkservice is NULL of " + dg.name + ", just return TRUE");
            return true;
        }
        String serviceName = dg.name;
        //eureka: port, Zuul: port+1
        //TODO: it is misunderstanding here!
        Integer port = Integer.valueOf(dg.nodePort) - 1;
        ApiServerCmd apiServerCmd = new ApiServerCmd();

        logger.debug("Release DG:" + dg.toString());
        logger.debug("Release DG Step 1: delete service and Step 2 put port back");
        if(! apiServerCmd.deleteService(serviceName, port, flag)){
            logger.error("Failed to release DG: " + dg.toString());
            return false;
        }
        //
        String dgMongoIp = dg.bkService.mongoIP;

        ControllerCoreApplication.bkServiceNameMap.remove(dg.bkService.name);
        dg.bkService.imoName = null;
        dg.bkService.dgName = null;


        //TODO: change all node + "+" + type to function call
        logger.debug("Release DG Step 3: put backservice to Readypool");
        String nodeType = dg.node + "+" + dg.type;
        logger.trace("Before release DG,  bkServiceReadyPoolMap of " + nodeType + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).toString());
        ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).add(dg.bkService);
        dg.bkService = null;
        logger.trace("After release DG,  bkServiceReadyPoolMap of " + nodeType + " is: " + ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).toString());

        logger.debug("Release DG Step 2: cleanMongoDB");
        MongoCmd.cleanMongo(dgMongoIp);


        logger.debug("Release DG Step 4: remove DG from DGlist");
        logger.trace("Before release DG: " + dg.name + ", IMO is " + imo.toString());
        //TODO: should I remove DG from dgList in DgCmds.releaseDG()???
        imo.dgList.remove(dg);
        logger.debug("After release DG: " + dg.name + ", IMO is " + imo.toString());
        return true;
    }

}
