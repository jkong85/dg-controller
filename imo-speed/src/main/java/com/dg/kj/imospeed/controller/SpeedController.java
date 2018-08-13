package com.dg.kj.imospeed.controller;

import com.dg.kj.imospeed.ImoSpeedApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeedController {
    private static final Logger logger = LogManager.getLogger(SpeedController.class);

    @Autowired
    private ImoSpeedApplication imoSpeedApplication;
    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                        @RequestParam String type,
                        @RequestParam String value){
        imoSpeedApplication.speedHistoryData.add(0, value);

//        imoSpeedApplication.logQueue.offer("receive speed data: " + value);
        imoSpeedApplication.type = type;
        return "cur speed is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Speed history data is: <br/>" + imoSpeedApplication.speedHistoryData;
        return result;
    }
    @RequestMapping(value="/ready")
    public String ready(){
        return imoSpeedApplication.curServiceName + " is ready";
    }

    @RequestMapping(value="/cleanrun")
    private void cleanRuntime(){
        logger.debug("Clean the runtime information");
        logger.debug("Before cleaning, the size of location history data: " + ImoSpeedApplication.speedHistoryData.size());
        ImoSpeedApplication.logQueue.clear();
        ImoSpeedApplication.speedHistoryData.clear();
        logger.debug("After cleaning, the size of location history data: " + ImoSpeedApplication.speedHistoryData.size());
    }
}
