package com.dg.kj.imolocation;

import com.dg.kj.dgcommons.DgCommonsApplication;
import com.dg.kj.dgcommons.Log;
import com.dg.kj.dgcommons.MongoOps;
import com.dg.kj.imolocation.controller.LocationController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class MigrationCopy implements Runnable {
    private static final Logger logger = LogManager.getLogger(MigrationCopy.class);
    private Thread t;
    private String threadName;

    private static final String CORE_NODE = "core";
    private static final String EDGE_NODE1 = "edge1";
    private static final String EDGE_NODE2 = "edge2";

    private static final String CONTROLLER_MIGRATE_URL = "http://172.17.8.101:30002/core/migration";
    private static final String CONTROLLER_DEL_DG_URL = "http://172.17.8.101:30002/core/deletedg";
    private static final String CONTROLLER_DEL_IMO_URL = "http://172.17.8.101:30002/core/deleteimo";

    private static Boolean isLeftRight = false;
    private static Boolean isRightLeft = false;


    String curServiceName = null;
    String curNode = null;
    String mongoIp = null;

    MigrationCopy(){
        curServiceName = System.getenv("SERVICE_LABEL"); //TODO: take care here, it is Bkservice name, NOT DG's name
        curNode = System.getenv("CUR_NODE");
        mongoIp = System.getenv("MONGODB_IP");
    }
    MigrationCopy(String name){
        threadName = name;

        curServiceName = System.getenv("SERVICE_LABEL"); //TODO: take care here, it is Bkservice name, NOT DG's name
        curNode = System.getenv("CUR_NODE");
        mongoIp = System.getenv("MONGODB_IP");
    }

    public void run() {
        //TODO: add type ENV to determine car's type
        String type = "honda";
        if(curServiceName.substring(0, 1).equals("h")){
            type = "honda";
        }else if(curServiceName.substring(0, 1).equals("t")){
            type = "toyota";
        }

        while(true) {
            //TODO: develop your own migration algorithm
            //oldMigrateLogic(curServiceName, curNode, type);
            migrateLogic(curServiceName, curNode, type);
            DgCommonsApplication.delay(1);
        }

    }
    //check the direction of car based on location data
    private boolean lefttoRight(){
        if(ImoLocationApplication.locationHistoryData.size()>6) {
            Integer preLocation = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(5));
            Integer location = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(0));
            return preLocation < location ? true : false;
        }
        return false;
    }

    private void migrateLogic(String curServiceName, String curNode, String type){
        if(ImoLocationApplication.locationHistoryData.size() < 10){
            return;
        }
        Integer location = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(0));
        if (location >= 20 && location < 30) {
            if (lefttoRight() && curNode.equals(EDGE_NODE1)) {
                if(!isLeftRight) {
                    migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
                    isLeftRight = true;
                }
            } else if((!lefttoRight()) && curNode.equals(EDGE_NODE2)){
                if(! isRightLeft) {
                    migrate(curServiceName, type, EDGE_NODE2, EDGE_NODE1);
                    isRightLeft = true;
                }
            }
        }else{
            isLeftRight = false;
            isRightLeft = false;
        }
        if(location >= 110){
            System.out.println(" Leaving the cloud, destory all DGs of " + curServiceName);
            //deleteDG(curServiceName, curNode, type);
            deleteIMO(curServiceName);
        }
    }

    public void start () {
        System.out.println("Starting MigrationCopy of " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
    private void migrate(String name, String type, String src, String dst){
        logger.debug("Migrate " + name + " from node " + src + " to node " + dst);
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> copyParamMap = new LinkedMultiValueMap<String, Object>();
        copyParamMap.add("bkname", name);
        copyParamMap.add("type", type);
        copyParamMap.add("srcNode", src);
        copyParamMap.add("dstNode", dst);

        logger.debug("Try to migrate DG form " + src + " to " + dst);
        ImoLocationApplication.logQueue.offer("Migrate " + name + " from node " + src + " to node " + dst);
        boolean retry = true;
        int cnt = 5;
        while(retry && cnt>0){
            try {
                template.postForObject(CONTROLLER_MIGRATE_URL, copyParamMap, String.class);
                logger.debug("Migrate DG from " + src + " to " + dst + " successfully!");
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            DgCommonsApplication.delay(1);
            cnt--;
        }
        if(retry == true){
            ImoLocationApplication.logQueue.offer("Failed to migrate DG of " + name + " from " + src + " to " + dst);
            logger.error("Failed to migrate DG of " + name + " from " + src + " to " + dst);
            return;
        }
        // Clean the runtime data in order to be ready for others to use
        // Wait for several seconds
        DgCommonsApplication.delay(5);
        cleanRuntime();
        //clean other micro-service runtime
        LocationController locationController = new LocationController();
        locationController.cleanOtherRuntime("http://speed/cleanrun");
        //clean MongoDB
        logger.debug("Clean the MongoDb data => MongoDB IP: " + mongoIp);
        MongoOps.cleanMongo(mongoIp);

        Log log = new Log("location", name, 3);
        log.logUpload("Migrate DG of " + name + " from " + src + " to " + dst);
    }
    //Clean the runtime information for other DGs
    //TODO: Send to other micro-service components to clean the runtime
    private void cleanRuntime(){
        logger.debug("Clean the runtime information");
        logger.debug("Before cleaning, the size of location history data: " + ImoLocationApplication.locationHistoryData.size());
        ImoLocationApplication.locationHistoryData.clear();
        ImoLocationApplication.logQueue.clear();
        logger.debug("After cleaning, the size of location history data: " + ImoLocationApplication.locationHistoryData.size());
    }

    //delete the DG that uses current BackupService with name : bkname
    private void deleteDG(String bkname, String type, String node){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("name", bkname);
        destroyParamMap.add("type", type);
        destroyParamMap.add("node", node);

        boolean retry = true;
        int cnt = 5;
        while(retry && cnt>0){
            try {
                template.postForObject(CONTROLLER_DEL_DG_URL, destroyParamMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{ Thread.sleep(200); }catch (InterruptedException ie){ }
            cnt--;
        }
        Log log = new Log("location", bkname, 3);
        log.logUpload("Delete myself DG on " + node);
    }

    //delete the IMO and its DGs that uses current BackupService with name : bkname
    private void deleteIMO(String bkname){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("name", bkname);

        boolean retry = true;
        int cnt = 5;
        while(retry && cnt>0){
            try {
                String response = template.postForObject(CONTROLLER_DEL_IMO_URL, destroyParamMap, String.class);
                if(!(response == null)) {
                    System.out.println(response);
                    retry = false;
                }
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{ Thread.sleep(200); }catch (InterruptedException ie){ }
            cnt--;
        }
        Log log = new Log("location", bkname, 3);
        log.logUpload("Delete IMO of " + bkname);
    }

}
