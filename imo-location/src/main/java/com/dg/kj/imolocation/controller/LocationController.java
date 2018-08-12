package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationController {
    private static final Logger logger = LogManager.getLogger(LocationController.class);

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
        // save it to MongoDB
        //We have defined in application.properties: spring.data.mongodb.uri=mongodb://${MONGODB_IP:localhost}:27017/test
        // All data is saved to DB: test
        mongoTemplate.save(new LocationData(imoLocationApplication.locationHistoryData.size(), value), "LocationData");
        return "Current location is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Location history data is: <br/>" + imoLocationApplication.locationHistoryData;
        return result;
    }
    @RequestMapping(value="/ready")
    public String ready(){
        logger.info("Service : imo-location is ready now!");
        return imoLocationApplication.curServiceName + " is ready";
    }
    // Define the data saved to MongoDB
    private class LocationData{
        Integer index;
        String value;
        public LocationData(Integer index, String value){
            this.index = index;
            this.value = value;
        }
    }
}
