package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController {
    @Autowired
    private ImoLocationApplication imoLocationApplication;
    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                          @RequestParam String type,
                          @RequestParam String value){

        imoLocationApplication.locationHistoryData.add(0, Integer.valueOf(value));
        imoLocationApplication.logQueue.offer(Integer.valueOf(value));

        return "Current location is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Location history data is: " + imoLocationApplication.locationHistoryData.toString();
        return result;
    }
}
