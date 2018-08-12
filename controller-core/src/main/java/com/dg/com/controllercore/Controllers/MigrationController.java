package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.DgCmds;
import com.dg.com.controllercore.Tasks.MongoCmd;
import com.dg.kj.dgcommons.DgCommonsApplication;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class MigrationController {
    private static final Logger logger = LogManager.getLogger(MigrationController.class);
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;
    @Autowired
    private LogController logController;

    @RequestMapping(value = "/migration")
    public String migrate(@RequestParam String bkname,    //TODO: It is Backupservice name, NOT DG's name
                           @RequestParam String type,
                           @RequestParam String srcNode,
                           @RequestParam String dstNode) {
        logger.info("Migration request of DG binded to BkService " + bkname + " from: " + srcNode + " to " + dstNode);
        if(! controllerCoreApplication.bkServiceNameMap.containsKey(bkname)){
            logger.error(" Failed to find BkService with name: " + bkname);
            return null;
        }
        String imoName = controllerCoreApplication.bkServiceNameMap.get(bkname).imoName;
//        String dgName = controllerCoreApplication.bkServiceNameMap.get(bkname).dgName;
        if(controllerCoreApplication.IMOMap == null || imoName == null || !controllerCoreApplication.IMOMap.containsKey(imoName)){
            return "IMO is NOT existed for " + bkname;
        }

        IMO imo = controllerCoreApplication.IMOMap.get(imoName);
        DG srcDG = imo.findDGonNode(srcNode);
        if(srcDG == null){
            logger.warn("Src DG is NOT existed! DO nothing!");
            return "DG on srcNode " + srcNode + " is NOT existed! ";
        }

        //Step 1: Check whether there is DG on dstNode, if not, find and bind one available BackupService
        // We assume that there is ONLY ONE DG for each IMO on each node
        if(dstNode.equals(imo.findDGonNode(dstNode))){
            logger.warn(" DG for " + imoName + " existed on node " + dstNode + "=>" + imo.findDGonNode(dstNode).toString());
            return null;
        }
        String dstServiceName = imoName + "-" + dstNode;
        DG dstDG = DgCmds.createDGQuick(dstServiceName, imo, type, dstNode);
        if(dstDG == null){
            logger.warn("Failed to create a new DG for " + imoName + " on node " + dstNode);
            return null;
        }

        //Step 2: Data migration
        if(!MongoCmd.migrateMongoDB(srcDG, dstDG)){
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

    @RequestMapping(value = "/deletedg")
    public String deletedg(@RequestParam String bkname,
                          @RequestParam String type,
                          @RequestParam String node) {
        logger.info("Delete DG binded to BkService " + bkname );
        String imoName = controllerCoreApplication.bkServiceNameMap.get(bkname).imoName;
        if(controllerCoreApplication.IMOMap == null || imoName == null || !controllerCoreApplication.IMOMap.containsKey(imoName)){
            return "IMO is NOT existed for " + bkname;
        }
        IMO imo = controllerCoreApplication.IMOMap.get(imoName);
        DG dg = imo.findDGonNode(node);
        if(dg == null){
            logger.warn("current DG is NOT existed! DO nothing!");
            return "DG on cloud " + node + " is NOT existed! ";
        }
        if(! DgCmds.releaseDG(imo, dg, true)) {
            logger.error("Failed to destroy DG " + dg.toString());
            return null;
        }
        return "Destroy DG " + dg.name + " on cloud " + node + " successfully!";
    }

    @RequestMapping(value = "/deleteimo")
    public String deleteimo(@RequestParam String bkname) {
        logger.info("Delete IMO binded to BkService " + bkname);
        String imoName = controllerCoreApplication.bkServiceNameMap.get(bkname).imoName;
        if(controllerCoreApplication.IMOMap == null || imoName == null || !controllerCoreApplication.IMOMap.containsKey(imoName)){
            return "IMO is NOT existed for " + bkname;
        }
        IMO imo = controllerCoreApplication.IMOMap.get(imoName);

        logger.debug("IMO of " + imoName + " => " + imo.toString());
        for(int i=imo.dgList.size()-1; i>=0; i--) {
            //TODO: should I remove DG from dgList in DgCmds.releaseDG()???, Take care of ArrayList remove , Iterator may be better??
            DG dg = imo.dgList.get(i);  // dgList will be changed in relasesDG(), so we delete the first all the time
            if (dg == null) {
                logger.warn("current DG is NOT existed! continue!");
            }else if (! DgCmds.releaseDG(imo, dg, true)) {  // try to release DG
                logger.error("Failed to destroy one DG of " +imo + " ==> " + dg.toString());
                return null;
            }
        }
        controllerCoreApplication.IMOMap.remove(imoName);
        return "Destroy IMO " + imoName + " on all cloud nodes successfully!";
    }

}
