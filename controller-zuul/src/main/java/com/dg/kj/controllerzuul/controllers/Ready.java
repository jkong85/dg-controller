package com.dg.kj.controllerzuul.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by jkong on 8/11/18.
 */
@RestController
public class Ready {
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/ready")
    public String checkReady(){
        try {
            restTemplate.getForObject("http://location/ready", String.class);
            return "ready!";
        }catch (RestClientException re){
            return null;
        }
    }
}
