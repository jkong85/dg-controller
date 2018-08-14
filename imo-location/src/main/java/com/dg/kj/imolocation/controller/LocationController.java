package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class LocationController {
    private static final Logger logger = LogManager.getLogger(LocationController.class);

    @Autowired
    private ImoLocationApplication imoLocationApplication;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                          @RequestParam String type,
                          @RequestParam String value){
        imoLocationApplication.locationHistoryData.add(0, value);
        imoLocationApplication.logQueue.offer("receive location data: " + value);

        // save it to MongoDB
        // We have defined in application.properties: spring.data.mongodb.uri=mongodb://${MONGODB_IP:localhost}:27017/test
        // All data is saved to DB: test
        mongoTemplate.save(new LocationData(imoLocationApplication.locationHistoryData.size(), value), "LocationData");
        return "Current location is: " + value;
    }

    // get the runtime data
    @RequestMapping(value="/history")
    public String history(){
        logger.trace("Request for runtime history: <br/>");
        String result = "Location history data is: <br/>" + imoLocationApplication.locationHistoryData;
        return result;
    }
    // get the env
    @RequestMapping(value="/info")
    public String info(){
        logger.trace("Request for information: <br/>");
        String result = "Information is: <br/>" ;
        result += "BkService: " + ImoLocationApplication.curServiceName + "<br/>";
        result += "Cur Node: " + ImoLocationApplication.curNode+ "<br/>";
        result += "MongoDB IP: " + ImoLocationApplication.mongIP+ "<br/>";
        logger.trace(result);
        return result;
    }

    // get the data in MongoDB
    @RequestMapping(value="/dbhistory")
    public String dbhistory(){
        logger.trace("Request for db history: <br/>");
        List<LocationData> res = mongoTemplate.findAll(LocationData.class, "LocationData");
        String prefix = "Data on cloud node " + ImoLocationApplication.curNode + " : ";
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("<br/>");
        for(int i=res.size()-1; i>=0; i--){
//            sb.append("index: ");
//            sb.append(res.get(i).index);
//            sb.append(", data :");
            sb.append( prefix + res.get(i).value);
            sb.append("<br/>");
        }
        logger.trace(sb.toString());
        return sb.toString();
    }

    @RequestMapping(value="/ready")
    public String ready(){
        logger.info("imo-location micro-service is ready now!");
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
    @RequestMapping(value="/cleanruntest")
    public String cleanruntest(@RequestParam String service) {
        String url = "http://" + service + "/cleanrun";
        logger.debug(" Receive the request to clean runtime of " + service + " with URL: " + url);
        return cleanOtherRuntime(url);
    }
    //url = "http://speed/cleanrun"
    public String cleanOtherRuntime(String url){
        if(url == null){
            logger.warn(" URL in cleanOtherRuntime is null ");
            return null;
        }
        logger.debug(" URL in cleanOtherRuntime is " + url);
        boolean ok = false;
        for(Integer cnt = 0; cnt < 5; cnt++) {
            try {
                if(this.restTemplate ==null ){
                    logger.error(" restTemplate is NULL !");
                    return null;
                }
                String response = this.restTemplate.getForObject(url, String.class);
                ok = true;
                logger.debug("Successful clean runtime of service : " + url + " with response: " + response);
                break;
            } catch (RestClientException re) {
                logger.warn("Failed to clean runtime of service : " + url + " with response: " + re.toString());
            }
        }
        if(ok){
            return "Clean runtime of service " + url;
        }
        return null;
    }

}
