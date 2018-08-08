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

        // register on core cloud node
        if(ControllerCoreApplication.IMOMap.containsKey(name)){
            //TODO: return IP address directly
            logger.debug(name + " is already registered!");
            return "It is already registered!";
        }

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        IMO curIMO = new IMO(name, type);

        // for core cloud node
        String coreNode = ControllerCoreApplication.CORE_NODE;
        String coreServiceName = name + "-" + coreNode;
        String coreNodeType = coreNode + "+" + type;
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).isEmpty()){
            logger.debug("No available DG on core cloud, just wait!");
            return "No available DG on core cloud, wait!";
        }
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).isEmpty()){
            logger.debug("No available DG on core cloud, just wait!");
            return "No available DG on core cloud, wait!";
        }
        BackupService coreBackupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(coreNodeType).get(0);

        Integer core_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer core_node_port_zuul = core_node_port_eureka + 1;
        apiServerCmd.CreateService(name, coreBackupService.selector, core_node_port_eureka.toString(), core_node_port_zuul.toString());
        String coreIP = ControllerCoreApplication.nodeIPMap.get(coreNode);
        DG coreDG = new DG(coreServiceName, type, coreNode, coreIP, core_node_port_zuul.toString(), coreBackupService);
        curIMO.dgList.add(coreDG);

        ControllerCoreApplication.IMOMap.put(name, curIMO);

        // for Edge cloud node
        String edgeNode = getNodeByLocation(location);
        String edgeNodeType = edgeNode + "+" + type;
        // Get the backupservce from ready pool
        if(ControllerCoreApplication.bkServiceReadyPoolMap.get(edgeNodeType).isEmpty()){
            logger.warn("No available DG on edge cloud: " + edgeNode +  ", just wait!");
            return "No available DG on core cloud, wait!";
        }
        BackupService edgeBackupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(edgeNodeType).get(0);

        String edgeServiceName = name + "-" + edgeNode;
        Integer edge_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer edge_node_port_zuul = edge_node_port_eureka + 1;
        apiServerCmd.CreateService(name, edgeBackupService.selector, edge_node_port_eureka.toString(), edge_node_port_zuul.toString());
        String edgeIP = ControllerCoreApplication.nodeIPMap.get(edgeNode);
        DG edgeDG = new DG(edgeServiceName, type, edgeNode, edgeIP, edge_node_port_zuul.toString(), edgeBackupService);
        ControllerCoreApplication.IMOMap.get(name).dgList.add(edgeDG);

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

        String coreIP = ControllerCoreApplication.nodeIPMap.get(node);

        DG dg = new DG(dgName, type, node, coreIP, node_port_zuul.toString(), backupService);
        return dg;
    }

    private String getNodeByLocation(String location){
         if(Integer.valueOf(location) < 60) {
            return ControllerCoreApplication.EDGE_NODE_1;
        }else return ControllerCoreApplication.EDGE_NODE_2;
    }

}
