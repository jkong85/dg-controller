package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.ApiServerCmd;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RegistrationController {
    private static final Logger logger = LogManager.getLogger(RegistrationController.class);
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;

    @Autowired
    private LogController logController;

    @RequestMapping(value = "/registration")
    public String register(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String location) {
        logger.info("Receive the registration request=>name: " + name + ", type: " + type + ", location: " + location);
        // register on core cloud node
        if(ControllerCoreApplication.IMOMap.containsKey(name)){
            //TODO: return IP address directly
            logger.warn(name + " is already registered!");
            //TODO: change the return with Status Code
            return "It is already registered!";
        }

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        IMO curIMO = new IMO(name, type);

        // for core cloud node
        String coreNode = ControllerCoreApplication.CORE_NODE;
        String coreServiceName = name + "-" + coreNode;
        String coreNodeType = coreNode + "+" + type;
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).isEmpty()){
            //TODO: add the previous process?
            logger.warn("No available DG on core cloud, just wait!");
            return "No available DG on core cloud, wait!";
        }
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).isEmpty()){
            logger.warn("No available DG on core cloud, just wait!");
            return "No available DG on core cloud, wait!";
        }
        BackupService coreBackupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).get(0);
        logger.debug("Find the BackupService on node : " + coreNode + " for request: " + name + " => " + coreBackupService.toString());

        Integer core_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer core_node_port_zuul = core_node_port_eureka + 1;

        apiServerCmd.CreateService(coreServiceName, coreBackupService.selector, core_node_port_eureka.toString(), core_node_port_zuul.toString());

        String coreIP = ControllerCoreApplication.nodeIpMap.get(coreNode);
        DG coreDG = new DG(coreServiceName, type, coreNode, coreIP, core_node_port_zuul.toString(), coreBackupService);
        curIMO.dgList.add(coreDG);
        ControllerCoreApplication.IMOMap.put(name, curIMO);
        logger.info("New DG is allocated for " + name + " on node: " + coreNode + " => " + coreDG.toString());


        // for Edge cloud node
        String edgeNode = getNodeByLocation(location);
        String edgeNodeType = edgeNode + "+" + type;
        // Get the backupservce from ready pool
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(edgeNodeType).isEmpty()){
            logger.warn("No available DG on edge cloud: " + edgeNode +  ", just wait!");
            return "No available DG on core cloud, wait!";
        }
        BackupService edgeBackupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(edgeNodeType).get(0);
        logger.debug(" Find the BackupService on node : " + edgeNode + " for request: " + name + " => " + coreBackupService.toString());

        String edgeServiceName = name + "-" + edgeNode;
        Integer edge_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer edge_node_port_zuul = edge_node_port_eureka + 1;
        String edgeIP = ControllerCoreApplication.nodeIpMap.get(edgeNode);

        apiServerCmd.CreateService(edgeServiceName, edgeBackupService.selector, edge_node_port_eureka.toString(), edge_node_port_zuul.toString());

        DG edgeDG = new DG(edgeServiceName, type, edgeNode, edgeIP, edge_node_port_zuul.toString(), edgeBackupService);
        ControllerCoreApplication.IMOMap.get(name).dgList.add(edgeDG);
        logger.info("New DG is allocated for " + name + " on node: " + edgeNode + " => " + edgeDG.toString());

        //TODO: change the return with Status Code
        return "Register successfully!";
    }
    private DG createDG(String dgName, String type, String node){
        String nodeType = node + "+" + type;
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).isEmpty()){
            logger.debug("No available DG on core cloud, just wait!");
            return null;
        }
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).isEmpty()){
            logger.debug("No available DG on core cloud, just wait!");
            return null;
        }
        BackupService backupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(nodeType).get(0);
        backupService.status = ControllerCoreApplication.BK_SERVICE_STATUS_USED;

        Integer node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer node_port_zuul = node_port_eureka + 1;

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(dgName, backupService.selector, node_port_eureka.toString(), node_port_zuul.toString());

        String coreIP = ControllerCoreApplication.nodeIpMap.get(node);

        DG dg = new DG(dgName, type, node, coreIP, node_port_zuul.toString(), backupService);
        logger.info("New DG is allocated for " + dgName+ " on node: " + node+ " => " + dg.toString());
        return dg;
    }

    private String getNodeByLocation(String location){
         if(Integer.valueOf(location) < 60) {
            return ControllerCoreApplication.EDGE_NODE_1;
        }else return ControllerCoreApplication.EDGE_NODE_2;
    }

}
