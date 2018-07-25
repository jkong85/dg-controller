package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.ApiServerCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RegistrationController {
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
            // return the IP address directly!
            return "It is already registerred!";
        }

        ApiServerCmd apiServerCmd = new ApiServerCmd();
        IMO curIMO = new IMO(name, type);

        // for core cloud node
        String coreNode = ControllerCoreApplication.CORE_NODE;
        String coreServiceName = name + "-" + coreNode;
        BackupService coreBackupService = ControllerCoreApplication.bkServicePoolMap.get(coreNode).get(type).pop();
        if(coreBackupService == null){
            return "No available DG on core cloud, wait!";
        }
        Integer core_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer core_node_port_zuul = core_node_port_eureka + 1;
        apiServerCmd.CreateService(name, coreBackupService.selector, core_node_port_eureka.toString(), core_node_port_zuul.toString());
        String coreIP = ControllerCoreApplication.nodeIPMap.get(coreNode);
        DG coreDG = new DG(coreServiceName, type, coreNode, coreIP, core_node_port_zuul.toString(), coreBackupService);
        curIMO.dgList.add(coreDG);

        ControllerCoreApplication.IMOMap.put(name, curIMO);

        // for Edge cloud node
        String edgeNode = getNodeByLocation(location);
        // Get the backupservce from pool
        BackupService edgeBackupService = ControllerCoreApplication.bkServicePoolMap.get(edgeNode).get(type).pop();
        if(edgeBackupService == null){
            return "No available DGs on edge cloud node, wait!";
        }

        String edgeServiceName = name + "-" + edgeNode;
        Integer edge_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer edge_node_port_zuul = edge_node_port_eureka + 1;
        apiServerCmd.CreateService(name, edgeBackupService.selector, edge_node_port_eureka.toString(), edge_node_port_zuul.toString());
        String edgeIP = ControllerCoreApplication.nodeIPMap.get(edgeNode);
        DG edgeDG = new DG(edgeServiceName, type, edgeNode, edgeIP, edge_node_port_zuul.toString(), edgeBackupService);
        ControllerCoreApplication.IMOMap.get(name).dgList.add(edgeDG);

        return "Register successfully!";
    }

    private String getNodeByLocation(String location){
         if(Integer.valueOf(location) < 60) {
            return ControllerCoreApplication.EDGE_NODE_1;
        }else return ControllerCoreApplication.EDGE_NODE_2;
    }

}
