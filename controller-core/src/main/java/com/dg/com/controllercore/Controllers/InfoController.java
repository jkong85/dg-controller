package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.DG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class InfoController {
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;

    @Autowired
    private LogController logController;

    @RequestMapping(value = "/information")
    public String register(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String location) {
        StringBuilder sb = new StringBuilder();
        for(DG dg : ControllerCoreApplication.IMOMap.get(name).dgList){
            sb.append(dg.nodeIP);
            sb.append(":");
            sb.append(dg.nodePort);
        }
        return sb.toString();
    }

}
