package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.ApiServerCmd;
import com.dg.com.controllercore.Tasks.DgCmds;
import com.dg.com.controllercore.Tasks.IMOBehavior;
import com.dg.kj.dgcommons.DgCommonsApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MigrationController {
    private static final Logger logger = LogManager.getLogger(MigrationController.class);
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;
    @Autowired
    private LogController logController;

    @RequestMapping(value = "/migration")
    public String migrate(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String srcNode,
                           @RequestParam String dstNode) {
        if(controllerCoreApplication.IMOMap == null || !controllerCoreApplication.IMOMap.containsKey(name)){
            return "IMO is NOT existed for " + name;
        }
        IMO imo = controllerCoreApplication.IMOMap.get(name);
        DG srcDG = imo.findDGonNode(srcNode);
        if(srcDG == null){
            logger.warn("Src DG is NOT existed! DO nothing!");
            return "DG on source node is NOT existed! ";
        }

        //Step 1: Check whether there is DG on dstNode, if not, find and bind one available BackupService
        // We assume that there is ONLY ONE DG for each IMO on each node
        if(dstNode.equals(imo.findDGonNode(dstNode))){
            logger.warn(" DG for " + name + " existed on node " + dstNode + "=>" + imo.findDGonNode(dstNode).toString());
            return null;
        }
        String dstServiceName = name + "-" + dstNode;
        DG dstDG = DgCmds.createDGQuick(dstServiceName, imo, type, dstNode);
        if(dstDG == null){
            logger.warn("Failed to create a new DG for " + name + " on node " + dstNode);
            return null;
        }

        //Step 2: Data migration
        if(! migrateMongoDB(srcDG, dstDG)){
            logger.error(" Failed to migrate MongoDB from " + srcNode + " to " + dstNode + " => " + " srcDG: " + srcDG.toString() + " || dstDG: "  + dstDG.toString());
            // release the new DG allocated
            if( !DgCmds.releaseDG(imo, dstDG, true)){
                logger.error("Failed to release dstDG after migrateMongoDB failed => " + dstDG.toString());
            }else{
                logger.error("Release dstDG successfully after migrateMongoDB failed => " + dstDG.toString());
            }
            return null;
        }

        //Step 3: Destroy the old one (release the BackupService, put the BackupServiceclean the MongoDB)

        if(! DgCmds.releaseDG(imo, srcDG, true)){
            logger.error("Failed to release srcDG => " + srcDG.toString());
            return null;
        }
        return "Migrate successfully!";
    }

    @RequestMapping(value = "/destroy")
    public String destroy(@RequestParam String name,
                          @RequestParam String type,
                          @RequestParam String node) {
        if(controllerCoreApplication.IMOMap == null || !controllerCoreApplication.IMOMap.containsKey(name)){
            return "IMO is NOT existed for " + name;
        }
        IMO imo = controllerCoreApplication.IMOMap.get(name);
        DG dg = imo.findDGonNode(node);
        if(dg == null){
            logger.warn("current DG is NOT existed! DO nothing!");
            return "DG on cloud " + node + " is NOT existed! ";
        }
        if(! DgCmds.releaseDG(imo, dg, true)) {
            logger.error("Failed to destroy DG " + dg.toString());
            return null;
        }
        return "Destroy DG " + name + " on cloud " + node + " successfully!";
    }

    //TODO: Add the MONGODB migration logic
    public boolean migrateMongoDB(DG srcDg, DG dstDG){
        if(srcDg == null || dstDG == null || srcDg == dstDG){
            logger.warn(" Source DG or Destination DG is null or SrcDG == DstDG , Do nothing!");
            return true;
        }
        DgCommonsApplication.delay(5);
        logger.debug("Successfully migrate MongoDb from " + srcDg.name + " to " + dstDG.name);
        return true;
    }
}
