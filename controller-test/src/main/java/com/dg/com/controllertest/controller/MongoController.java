package com.dg.com.controllertest.controller;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
