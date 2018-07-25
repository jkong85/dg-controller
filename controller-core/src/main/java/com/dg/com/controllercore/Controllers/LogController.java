package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LogController {

    @RequestMapping(value = "/logwrite")
    public String logWrite(@RequestParam String sender,
                           @RequestParam String log){
        AddLog(sender, log);
        return "write log successfully!";
    }

    public static void AddLog(String sender, String log){
        if(sender == null){
            return;
        }
        /*
        if(!DGCurLogMap.containsKey(sender)){
            List<String> logList = new ArrayList<>();
            logList.add("Start to show log.");
            DGCurLogMap.put(sender, logList);
        }

        if(!DGHistoryLogMap.containsKey(sender)){
            List<String> logList = new ArrayList<>();
            logList.add("Start to show log.");
            DGHistoryLogMap.put(sender, logList);
        }

        DGCurLogMap.get(sender).add(0, log);
        DGHistoryLogMap.get(sender).add(0, log);
        */
    }
}
