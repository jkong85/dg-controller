package com.dg.com.controllertest.controller;

import com.dg.com.controllertest.ControllerTestApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LogController {

    @RequestMapping(value = "/logwrite")
    public String logWrite(@RequestParam String sender,
                        @RequestParam String log){
        ControllerTestApplication.AddLog(sender, log);
        return "write log successfully!";
    }

    @RequestMapping(value = "/loginfo")
    public String logEntity(){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry entry : ControllerTestApplication.DGCurLogMap.entrySet()){
            sb.append(entry.getKey());
            sb.append("\n");
        }
        return sb.toString();
    }

    @RequestMapping(value = "/log")
    public String log(@RequestParam String sender){
        if(!ControllerTestApplication.DGCurLogMap.containsKey(sender) && ControllerTestApplication.DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is deleted!";
        }else if(!ControllerTestApplication.DGCurLogMap.containsKey(sender) && !ControllerTestApplication.DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is not ready yet!";
        }
        if(ControllerTestApplication.DGCurLogMap.get(sender).isEmpty()) {
            return "No log";
        }
        return ControllerTestApplication.DGCurLogMap.get(sender).get(0);
    }
    @RequestMapping(value = "/loghistory")
    public String logHistory(@RequestParam String sender){
        if(ControllerTestApplication.DGHistoryLogMap.containsKey(sender)){
            return "No log for " + sender + ", it is not ready yet!";
        }
        if(ControllerTestApplication.DGHistoryLogMap.get(sender).isEmpty()) {
            return "No log";
        }
        StringBuilder sb = new StringBuilder();
        for(String str : ControllerTestApplication.DGHistoryLogMap.get(sender)){
            sb.append(" From " + sender + " : " + str + "\n");
        }
        return sb.toString();
    }
}
