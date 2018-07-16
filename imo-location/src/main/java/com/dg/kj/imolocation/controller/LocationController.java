package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class LocationController {
    private static final String CORE_NODE = "node1";
    private static final String EDGE_NODE1 = "node2";
    private static final String EDGE_NODE2 = "node3";
    private static final String CONTROLLER_URL = "http://172.17.8.101:8080/test/info";
    @Autowired
    private ImoLocationApplication imoLocationApplication;
    @RequestMapping(value = "/cur")
    public String current(@RequestParam String name,
                          @RequestParam String type,
                          @RequestParam String value){

        imoLocationApplication.locationHistoryData.add(0, Integer.valueOf(value));

        // determine whether to migrate to other nodes
        //TODO: develop your own migration algorithm
        Integer location = Integer.valueOf(value);
        String migrateInfo = null;
        if(location == 40){
            migrate(name, type, EDGE_NODE1, EDGE_NODE2);
            migrateInfo = "Copy DG from " + EDGE_NODE1 + " to " +  EDGE_NODE2;
        }

        return "Current location is: " + value + "\n" + migrateInfo;
    }

    @RequestMapping(value="/history")
    public String history(){
        String result = "Location history data is: " + imoLocationApplication.locationHistoryData.toString();
        return result;
    }

    private void migrate(String name, String type, String src, String dst) throws HttpClientErrorException {
            RestTemplate template = new RestTemplate();
            MultiValueMap<String, Object> copyParamMap = new LinkedMultiValueMap<String, Object>();
            copyParamMap.add("name", name);
            copyParamMap.add("type", type);
            copyParamMap.add("srcNode", src);
            copyParamMap.add("dstNode", dst);

            String result = template.postForObject(CONTROLLER_URL, copyParamMap, String.class);
    }
}
