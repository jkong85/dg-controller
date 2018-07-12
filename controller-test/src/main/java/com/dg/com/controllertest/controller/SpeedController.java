package com.dg.com.controllertest.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeedController {
    @RequestMapping(value = "/speed")
    public String index(@RequestParam String value){
        return "Speed is: " + value;
    }

}
