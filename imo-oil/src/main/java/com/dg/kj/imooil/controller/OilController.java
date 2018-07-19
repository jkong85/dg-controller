package com.dg.kj.imooil.controller;

import com.dg.kj.imooil.ImoOilApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OilController {
    @Autowired
    private ImoOilApplication imoOilApplication;
    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                        @RequestParam String type,
                        @RequestParam String value){
        imoOilApplication.oilHistoryData.add(0, value);
        imoOilApplication.logQueue.offer(value);
        return "Current oil is: " + value;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Oil history data is: " + imoOilApplication.oilHistoryData;
        return result;
    }

    @RequestMapping(value="/ready")
    public String ready(){
        return imoOilApplication.curServiceName + "is ready";
    }
}
