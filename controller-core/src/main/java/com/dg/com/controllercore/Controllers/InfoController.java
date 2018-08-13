package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.IMO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class InfoController {
    private static final Logger logger = LogManager.getLogger(RegistrationController.class);
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;
    @Autowired
    private LogController logController;

    @RequestMapping(value = "/information")
    public String information(@RequestParam String name){
        logger.trace("/information request from IMO: " + name);
        if(controllerCoreApplication.IMOMap == null || !controllerCoreApplication.IMOMap.containsKey(name)){
            logger.warn("IMO : " + name + " do NOT existed!");
            return null;
        }
        String imoInfo = controllerCoreApplication.IMOMap.get(name).getAllDGIpPort().toString();
        logger.trace("Information for IMO : " + name + " is: " + imoInfo.toString());
        return imoInfo;
    }

    /*
    //deprecated
    @RequestMapping(value = "/infor")
    public String info(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String location) {
        logger.debug("/information request => name:" + name + ", type:" + type + ", location:" + location );
        if(controllerCoreApplication.IMOMap == null || !controllerCoreApplication.IMOMap.containsKey(name)){
            logger.warn("IMO : " + name + " do NOT existed!");
            return null;
        }
        String imoInfo = controllerCoreApplication.IMOMap.get(name).getAllDGIpPort().toString();
        logger.debug("/information for (name:" + name + ", type:" + type + ", location:" + location + ") is: " + imoInfo.toString());
        return imoInfo;
    }
    */


    //It is for debug: we can get the runtime information by API call
    //TODO: add more based on your demand
    @RequestMapping(value = "/runtimeinfo")
    public String runtimeinfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("===Runtime Information Requst===");
        sb.append(System.getProperty("line.separator"));
        sb.append("<br/>");
        sb.append(printIMOinfo());
        sb.append("<br/>");
        sb.append(System.getProperty("line.separator"));
        sb.append(printCurBkPool());
        return sb.toString();
    }

    private String printIMOinfo(){
        StringBuilder sb = new StringBuilder();
        sb.append("All IMO information: ");
        sb.append("<br/>");
        sb.append(System.getProperty("line.separator"));
        for(Map.Entry<String, IMO> cur : controllerCoreApplication.IMOMap.entrySet() ){
            sb.append("     " + cur.toString());
            sb.append(System.getProperty("line.separator"));
            sb.append("<br/>");
        }
        return sb.toString();
    }

    private String printPortPool(){
        StringBuilder sb = new StringBuilder();
        sb.append("==========Available Port Pool ======================" );
        sb.append(System.getProperty("line.separator"));
        sb.append("<br/>");
        sb.append(ControllerCoreApplication.nodePortsPool.toString());
        sb.append("<br/>");
        return sb.toString();
    }
    private String printCurBkPool(){
        StringBuilder sbReady = new StringBuilder();
        StringBuilder sbNotReady = new StringBuilder();
        for(String node : ControllerCoreApplication.NODE_LIST){
            for(String type : ControllerCoreApplication.IMO_TYPE) {
                String nodetype = node + "+" + type;
                sbReady.append("<br/>");
                sbReady.append(System.getProperty("line.separator"));
                sbReady.append(ControllerCoreApplication.bkServiceReadyPoolMap.get(nodetype).toString());
                sbNotReady.append("<br/>");
                sbNotReady.append(System.getProperty("line.separator"));
                sbNotReady.append(ControllerCoreApplication.bkServiceNotReadyPoolMap.get(nodetype).toString());
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("==========Current BackupServier Pools Info ==========" );
        sb.append(System.getProperty("line.separator"));
        sb.append("<br/>");
        sb.append("<br/>");
        sb.append(" ==> Current BackupRServier READY Pools are: ");
        sb.append(System.getProperty("line.separator"));
        sb.append(sbReady.toString());
        sb.append(System.getProperty("line.separator"));
        sb.append("<br/>");
        sb.append(" ==> Current BackupRServier NOT READY Pools are: ");
        sb.append(sbNotReady.toString());
        sb.append("<br/>");
        logger.debug(sb.toString());
        return sb.toString();
    }



}
