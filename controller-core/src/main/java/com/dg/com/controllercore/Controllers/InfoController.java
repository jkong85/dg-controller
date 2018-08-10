package com.dg.com.controllercore.Controllers;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.DG;
import com.dg.com.controllercore.IMOs.IMO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class InfoController {
    private static final Logger logger = LogManager.getLogger(RegistrationController.class);
    @Autowired
    private ControllerCoreApplication controllerCoreApplication;
    @Autowired
    private LogController logController;

    @RequestMapping(value = "/information")
    public String info(@RequestParam String name,
                           @RequestParam String type,
                           @RequestParam String location) {
        logger.debug("/information request => name:" + name + ", type:" + type + ", location:" + location );
        if(controllerCoreApplication.IMOMap == null || !controllerCoreApplication.IMOMap.containsKey(name)){
            logger.warn("IMO : " + name + " do NOT existed!");
            return null;
        }
        String imoInfo = controllerCoreApplication.IMOMap.get(name).getAllDGIpPort().toString();
        logger.debug("/information for (name:" + name + ", type:" + type + ", location:" + location + ") is: " + imoInfo.toString());
        return imoInfo;
    }

    @RequestMapping(value = "/globalinfo")
    public String globalinfo() {
        logger.debug("/global information requst");
        StringBuilder sb = new StringBuilder();
        //public static Map<String, IMO> IMOMap = new HashMap<>();
        sb.append("All IMO information: ");
        for(Map.Entry<String, IMO> cur : controllerCoreApplication.IMOMap.entrySet() ){
            sb.append("     " + cur.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


}
