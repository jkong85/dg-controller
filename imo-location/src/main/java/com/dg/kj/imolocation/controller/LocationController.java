package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController {
    @Autowired
    private ImoLocationApplication imoLocationApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                          @RequestParam String type,
                          @RequestParam String value){
        imoLocationApplication.locationHistoryData.add(0, value);
        imoLocationApplication.logQueue.offer("receive location data: " + value);
        mongoTemplate.save(new SpeedData(imoLocationApplication.locationHistoryData.size(), value), "SpeedData");
        return "Current location is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Location history data is: <br/>" + imoLocationApplication.locationHistoryData;
        return result;
    }
    @RequestMapping(value="/ready")
    public String ready(){
        return imoLocationApplication.curServiceName + " is ready";
    }

    private class SpeedData{
        Integer index;
        String value;
        public SpeedData(Integer index, String value){
            this.index = index;
            this.value = value;
        }
    }
    private class SpeedLog{
        Integer index;
        String log;
        public SpeedLog(Integer index, String log){
            this.index = index;
            this.log = log;
        }
    }
}
