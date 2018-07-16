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
        imoSpeedApplication.speedHistoryData.add(0, Integer.valueOf(value));
        Integer total = 0;
        for(int i=0; i<imoSpeedApplication.speedHistoryData.size(); i++){
            total += imoSpeedApplication.speedHistoryData.get(i);
        }

        return "cur speed is: " + value + ", total sum is :" + total.toString();
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Speed history data is: " + imoSpeedApplication.speedHistoryData.toString();
        return result;
    }
}
