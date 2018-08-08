package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.Tasks.ApiServerCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MigrationController {
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;

    @Autowired
    private LogController logController;

    @RequestMapping(value = "/migration")
    public String migrate(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String srcNode,
                           @RequestParam String dstNode) {

        return "Migrate successfully!";
    }

    @RequestMapping(value = "/copy")
    public String copy(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String srcNode,
                           @RequestParam String dstNode) {
        if(srcNode.equals(dstNode)){
            return "Src node is the same with dstNode!";
        }

        if(! ControllerCoreApplication.IMOMap.get(dstNode).dgList.isEmpty()){
            return "There is an existing DG on node " + dstNode + " ! No need to create a new one";
        }
        // find out an availale backup service
        BackupService edgeBackupService = ControllerCoreApplication.bkServiceReadyPoolMap.get(dstNode).get(type).pop();
        if(edgeBackupService == null){
            return "No available DGs on edge cloud node, wait!";
        }
        String edgeServiceName = name + "-" + dstNode;
        Integer edge_node_port_eureka = ControllerCoreApplication.nodePortsPool.pop();
        Integer edge_node_port_zuul = edge_node_port_eureka + 1;
        ApiServerCmd apiServerCmd = new ApiServerCmd();
        apiServerCmd.CreateService(name, edgeBackupService.selector, edge_node_port_eureka.toString(), edge_node_port_zuul.toString());
        String edgeIP = ControllerCoreApplication.nodeIPMap.get(dstNode);
        DG edgeDG = new DG(edgeServiceName, type, dstNode, edgeIP, edge_node_port_zuul.toString(), edgeBackupService);
        ControllerCoreApplication.IMOMap.get(name).dgList.add(edgeDG);
        return "Copy successfully!";
    }

}
