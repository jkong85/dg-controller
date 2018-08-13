package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.DgCmds;
import com.dg.com.controllercore.Tasks.IMOBehavior;
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
        logger.info("Receive the registration request (name:" + name + ", type:" + type + ",location: " + location + ")");
        LogController.writeLog(LogController.LOG_CONTROLLER, "Receive the registration request (name:" + name + ", type:" + type + ",location: " + location + ")");
        // register on core cloud node
        if(ControllerCoreApplication.IMOMap.containsKey(name)){
            //TODO: return IP address directly
            logger.warn(name + " is already registered!");
            //TODO: change the return with Status Code
            return "It is already registered!";
        }

        IMO curIMO = new IMO(name, type);

        // for core cloud node
        String coreNode = ControllerCoreApplication.CORE_NODE;
        String coreServiceName = name + "-" + coreNode;
        DG coreDG = DgCmds.createDGQuick(coreServiceName, curIMO, type, coreNode);
        if(coreDG == null){
            //TODO: check the logic here
            logger.warn("Failed to allocate DG for IMO: " + curIMO.toString() + " on node " + coreNode + "! Try createDGSlow way!");
            DgCmds.createDGSlow(coreServiceName, curIMO, type, coreNode);
        }
        logger.info("New DG is allocated for " + name + " on node: " + coreNode + " => " + coreDG.toString());
        LogController.writeLog(LogController.LOG_CONTROLLER, "A new DG is creating for " + name + " on node: " + coreNode);

        // for Edge cloud node
        String edgeNode = IMOBehavior.getNodeByLocation(location);
        String edgeServiceName = name + "-" + edgeNode;
        DG edgeDG =  DgCmds.createDGQuick(edgeServiceName, curIMO, type, edgeNode);
        if(edgeDG == null){
            //TODO: check the logic here
            logger.warn("Failed to allocate DG for IMO: " + curIMO.toString() + " on node " + edgeNode + "! Try createDGSlow way!");
            DgCmds.createDGSlow(edgeServiceName, curIMO, type, edgeNode);
        }
        logger.info("New DG is allocated for " + name + " on node: " + edgeNode + " => " + edgeDG.toString());
        LogController.writeLog(LogController.LOG_CONTROLLER, "A new DG is creating for " + name + " on node: " + edgeNode);

        // Finally, register to the controller
        if(coreDG != null && edgeDG != null) {
            ControllerCoreApplication.IMOMap.put(name, curIMO);
            logger.info("Successfully register DGs for IMO: " + curIMO.toString());
            LogController.writeLog(LogController.LOG_CONTROLLER, "Successfully Registered DGs for IMO:" + curIMO.name);
        }else{
            logger.warn("Failed to register DGs for IMO: " + curIMO.toString());
        }

        //TODO: change the return with Status Code
        return "Register successfully!";
    }
}
