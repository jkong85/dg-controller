package com.dg.com.controllertest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LogController {
    public static List<String> logList = new ArrayList<>();
    public static Map<String, List<String>> dgLogMap = new HashMap<>();

    @RequestMapping(value = "/logwrite")
    public String logWrite(@RequestParam String sender,
                        @RequestParam String log){
        if(dgLogMap.containsKey(sender)){
            dgLogMap.get(sender).add(0, log);
        }else{
            List<String> newLog = new ArrayList<>();
            newLog.add(0, log);
            dgLogMap.put(sender, newLog);
        }
        return "write log successfully!";
    }

    @RequestMapping(value = "/logcontroller")
    public String logcontroller(){
        if(logList ==null || logList.size()==0){
            return "No logs";
        }
        return logList.get(logList.size()-1);
    }

    @RequestMapping(value = "/logmap")
    public String logEntity(){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry entry : dgLogMap.entrySet()){
            sb.append(entry.getKey());
            sb.append("\n");
        }
        return sb.toString();
    }
    @RequestMapping(value = "/log")
    public String log(@RequestParam String sender){
        if(!dgLogMap.containsKey(sender) || dgLogMap.get(sender).isEmpty()){
            return "No logs";
        }else{
            return dgLogMap.get(sender).get(0);
        }
    }
}
