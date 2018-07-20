package com.dg.kj.imospeed.controller;

import com.dg.kj.imospeed.ImoSpeedApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpeedController {
    @Autowired
    private ImoSpeedApplication imoSpeedApplication;
    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                        @RequestParam String type,
                        @RequestParam String value){
        imoSpeedApplication.speedHistoryData.add(0, value);
        imoSpeedApplication.type = type;
        return "cur speed is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Speed history data is: " + imoSpeedApplication.speedHistoryData;
        return result;
    }
    @RequestMapping(value="/ready")
    public String ready(){
        return imoSpeedApplication.curServiceName + " is ready";
    }
}
