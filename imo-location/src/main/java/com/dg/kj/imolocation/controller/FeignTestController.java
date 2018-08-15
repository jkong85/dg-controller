package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.feignclient.ServiceSpeedFeignclient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jkong on 8/15/18.
 */
@RestController
public class FeignTestController {
    @Autowired
    private ServiceSpeedFeignclient serviceSpeedFeignclient;

    //This is a test for micro-service internal call
    //In this case, Location micro-service will call speed service
    //Refer to feignclient/ServiceSpeedFeignclient.java
    @RequestMapping(value="/feigntest")
    public String getFeigntest(){
        return "Location call Speed service, and the Speed service reponse: " + serviceSpeedFeignclient.feigntest().toString();
    }
}
