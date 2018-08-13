package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LogController {
    private static final Logger logger = LogManager.getLogger(LogController.class);
    public static Map<String, List<String>> DGCurLogMap = new HashMap<>();
    public static Map<String, List<String>> DGHistoryLogMap = new HashMap<>();

    public static final String LOG_CONTROLLER = "controller";

    public LogController(){
        DGCurLogMap = new HashMap<>();
        DGHistoryLogMap = new HashMap<>();
    }

    @RequestMapping(value = "/logwrite")
    public String logWrite(@RequestParam String sender,
                           @RequestParam String log){
        logger.debug("logwrite: write log of " + sender + ": " + log);
        sender = "honda1";
        return writeLog(sender, log);
    }
    @RequestMapping(value = "/bklogwrite")
    public String bklogWrite(@RequestParam String bksender,
                           @RequestParam String log){
        String sender = getRealSender(bksender);
        logger.debug("bklogwrite: write log of " + sender + ": " + log);
        sender = "honda1";
        return writeLog(sender, log);
    }

    @RequestMapping(value = "/bklogclean")
    public String bklogClean(@RequestParam String bksender){
        String sender = getRealSender(bksender);
        logger.debug("Clean log of " + sender);
        sender = "honda1";
        return cleanLog(sender);
    }
    @RequestMapping(value = "/logclean")
    public String logClean(@RequestParam String sender){
        logger.debug("Clean log of " + sender);
        sender = "honda1";
        return cleanLog(sender);
    }

    @RequestMapping(value = "/log")
    public String log(@RequestParam String sender){
        logger.debug("Get log of " + sender);
        sender = "honda1";
        return getLog(sender);
    }
    @RequestMapping(value = "/bklog")
    public String bklog(@RequestParam String bksender){
        String sender = getRealSender(bksender);
        logger.debug("Get log of " + sender);
        sender = "honda1";
        return getLog(sender);
    }

    @RequestMapping(value = "/loghistory")
    public String logHistory(@RequestParam String sender) {
        logger.debug("Get log history of " + sender);
        sender = "honda1";
        return getLogHistory(sender);
    }
    @RequestMapping(value = "/bkloghistory")
    public String bkLogHistory(@RequestParam String bksender) {
        String sender = getRealSender(bksender);
        logger.debug("Get log history of " + sender);
        sender = "honda1";
        return getLogHistory(sender);
    }
    @RequestMapping(value = "/logall")
    public String logAll() {
        logger.debug("All current log :" );
        if(DGCurLogMap == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, List<String>> entry: DGCurLogMap.entrySet()) {
            sb.append(" == " + entry.getKey());
            sb.append("<br/>");
            for(String cur : entry.getValue()){
                sb.append("====" + cur);
                sb.append("<br/>");
            }
        }
        return sb.toString();
    }
    @RequestMapping(value = "/loghistoryall")
    public String logHistoryAll() {
        logger.debug("All history log :" );
        if(DGHistoryLogMap == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, List<String>> entry: DGHistoryLogMap.entrySet()) {
            sb.append(" == " + entry.getKey());
            sb.append("<br/>");
            for(String cur : entry.getValue()){
                sb.append("====" + cur);
                sb.append("<br/>");
            }
        }
        return sb.toString();
    }

    private static String getRealSender(String bksender){
        logger.debug("Get bksender: " + bksender + " sender name. " );
        if(ControllerCoreApplication.bkServiceNameMap==null || !ControllerCoreApplication.bkServiceNameMap.containsKey(bksender) ){
            logger.warn("bksender: " + bksender + " sender name is: " + null);
            return null;
        }
        String sender = ControllerCoreApplication.bkServiceNameMap.get(bksender).imoName;
        logger.debug("Get bksender: " + bksender + " sender name is:  " + sender);
        return sender;
    }

    public static String writeLog(String sender, String log){
        if(sender == null){
            logger.warn("Failed to write log from " + sender);
            return "Failed to write log from " + sender;
        }
        if(!DGCurLogMap.containsKey(sender)){
            List<String> logList = new ArrayList<>();
            logList.add("Start to show log <br/>");
            DGCurLogMap.put(sender, logList);
        }

        if(!DGHistoryLogMap.containsKey(sender)){
            List<String> logList = new ArrayList<>();
            logList.add("Start to show log <br/>");
            DGHistoryLogMap.put(sender, logList);
        }

        DGCurLogMap.get(sender).add(0, log);
        DGHistoryLogMap.get(sender).add(0, log);

        return "write log successfully!";
    }
    private String cleanLog(String sender){
        if(sender == null){
            logger.warn("Failed to find log from " + sender);
            return "Failed to find log from " + sender;
        }
        if(DGCurLogMap!=null && DGCurLogMap.containsKey(sender)){
            DGCurLogMap.remove(sender);
        }
        if(DGHistoryLogMap!=null && DGHistoryLogMap.containsKey(sender)){
            DGHistoryLogMap.remove(sender);
        }
        return "clean log successfully!";
    }
     public String getLog(String sender){
        if(sender == null){
            logger.warn("Failed to find log from " + sender);
            return "Failed to find log from " + sender;
        }
        if(! DGCurLogMap.containsKey(sender) && DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is deleted!";
        }else if(! DGCurLogMap.containsKey(sender) && ! DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is not ready yet!";
        }
        if(DGCurLogMap.get(sender).isEmpty()) {
            return "No log";
        }
        return DGCurLogMap.get(sender).get(0);
    }
    public String getLogHistory(String sender) {
        if(sender == null){
            logger.warn("Failed to find log from " + sender);
            return "Failed to find log from " + sender;
        }
        if(! DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is not ready yet!";
        }
        if( DGHistoryLogMap.get(sender).isEmpty()) {
            return "No log";
        }
        StringBuilder sb = new StringBuilder();
        for(String str : DGHistoryLogMap.get(sender)){
            sb.append(" From " + sender + " : " + str);
            sb.append("<br/>");
        }
        return sb.toString();
    }

}
