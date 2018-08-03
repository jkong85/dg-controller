package com.dg.com.controllertest.controller;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
        mongoTemplate.insert(new logData(time, log));
        return "insert to DB";
    }
    @RequestMapping("/speed2")
    public String speed2(@RequestParam String time,
                        @RequestParam String log ){
        mongoTemplate.insert(new logData2(time, log));
        return "insert to DB";
    }
    @RequestMapping("/speedlog")
    public String speedlog(){
        Set<String> result  = mongoTemplate.getCollectionNames();
        StringBuilder sb = new StringBuilder();
        for(String str : result ){
            sb.append(str);
            sb.append(" ");
        }
        return sb.toString();
    }

    private class logData2{
        String timeStamp;
        String value;
        logData2(String time, String value){
            this.timeStamp = time;
            this.value = value;
        }
    }

    private class logData{
        String timeStamp;
        String value;
        logData(String time, String value){
            this.timeStamp = time;
            this.value = value;
        }
    }
}
