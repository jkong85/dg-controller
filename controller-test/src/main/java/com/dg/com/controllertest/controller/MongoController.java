package com.dg.com.controllertest.controller;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by jkong on 8/2/18.
 */

@RestController
public class MongoController {

    @Autowired
    MongoTemplate mongoTemplate;

    @RequestMapping("/mongo")
    public String mongo(){
        DB db = mongoTemplate.getDb();

        return db.getName();
    }
    @RequestMapping("/speed")
    public String speed(@RequestParam String time,
                        @RequestParam String log ){
        mongoTemplate.save(new logData(time, log), "log");
        return "insert to DB";
    }
    @RequestMapping("/speed2")
    public String speed2(@RequestParam String time,
                        @RequestParam String log ){
        mongoTemplate.save(new logData2(time, log), "log2");
        return "insert to DB";
    }
    @RequestMapping("/speedlog")
    public String speedlog(){
        return mongoTemplate.findAll(logData.class, "log").toString();
        /*
        Set<String> result  = mongoTemplate.getCollectionNames();
        StringBuilder sb = new StringBuilder();
        for(String str : result ){
            if(str.equals("logData")){
                List<logData> logdata = mongoTemplate.findAll(logData.class, str);
                sb.append(logdata.toString());
            }
        }
        return sb.toString();
        */
    }
    @RequestMapping("/speedlog2")
    public String speedlog2(){
        return mongoTemplate.findAll(logData2.class, "log2").toString();
        /*
        Set<String> result  = mongoTemplate.getCollectionNames();
        StringBuilder sb = new StringBuilder();
        for(String str : result ){
            if(str.equals("logData2")){
                List<logData> logdata = mongoTemplate.findAll(logData.class, str);
                sb.append(logdata.toString());
            }
        }
        return sb.toString();
        */
    }

    private class logData2{
        String time;
        String value;
        logData2(String time, String value){
            this.time= time;
            this.value = value;
        }
    }

    private class logData{
        String time;
        String value;
        logData(String time, String value){
            this.time= time;
            this.value = value;
        }
    }
}