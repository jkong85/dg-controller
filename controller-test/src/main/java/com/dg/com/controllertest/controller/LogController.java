package com.dg.com.controllertest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class LogController {
    public List<String> logList = new ArrayList<>();

    @RequestMapping(value = "/logwrite")
    public String getInfo(@RequestParam String sender,
                        @RequestParam String log){
        logList.add(sender + " : " + log);
        return null;
    }
    @RequestMapping(value = "/log")
    public String getLog(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<logList.size(); i++){
            sb.append(logList.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
