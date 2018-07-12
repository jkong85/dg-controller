package com.dg.com.controllertest.controller;

import com.dg.com.controllertest.ControllerTestApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeedController {
    @Autowired
    private ControllerTestApplication controllerTestApplication;

    @RequestMapping(value = "/cur")
    public String index(@RequestParam String value){
        controllerTestApplication.speedData.add(Integer.valueOf(value));
        return "cur peed is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        return "All history data is: " + controllerTestApplication.speedData.toString();
    }

}
