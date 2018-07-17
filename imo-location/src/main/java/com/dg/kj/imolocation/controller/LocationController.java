package com.dg.kj.imolocation.controller;

import com.dg.kj.imolocation.ImoLocationApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@RestController
public class LocationController {
    private static final String CORE_NODE = "node1";
    private static final String EDGE_NODE1 = "node2";
    private static final String EDGE_NODE2 = "node3";
    private static final String CONTROLLER_COPY_URL = "http://172.17.8.101:30002/test/copy";
    private static final String CONTROLLER_DESTROY_URL = "http://172.17.8.101:30002/test/destroy";
    private static boolean isMigrated = false;
    private static boolean isDestroyedofOld = false;
    private static boolean isDestroyedofNew = false;

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

        String curServiceName = System.getenv("SERVICE_LABEL");
        String curNode = System.getenv("CUR_NODE");

        if(location >= 40 && (!isMigrated)){
            migrate(name, type, EDGE_NODE1, EDGE_NODE2);
            migrateInfo = "Copy DG from " + EDGE_NODE1 + " to " +  EDGE_NODE2;
            isMigrated = true;
        }
        if(location > 60 && location < 100){

        }
        if(location >=100){
            // delete all edge DGs
            if(!isDestroyedofNew) {
                destroy(curServiceName, type, curNode);
            }
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

            String result = template.postForObject(CONTROLLER_COPY_URL, copyParamMap, String.class);
    }
    //destroy the DGs on node

//    public String destroy(@RequestParam String serviceName,
//                          @RequestParam String type,
//                          @RequestParam String node){
    private void destroy(String name, String type, String node){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("serviceName", name);
        destroyParamMap.add("type", type);
        destroyParamMap.add("node", node);

        String result = template.postForObject(CONTROLLER_DESTROY_URL, destroyParamMap, String.class);

    }
}
