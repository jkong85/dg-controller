package com.dg.kj.imolocation;

import com.dg.kj.dgcommons.DgCommonsApplication;
import com.dg.kj.dgcommons.Log;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class MigrationCopy implements Runnable {
    private Thread t;
    private String threadName;

    private static final String CORE_NODE = "core";
    private static final String EDGE_NODE1 = "edge1";
    private static final String EDGE_NODE2 = "edge2";

    private static final String CONTROLLER_COPY_URL = "http://172.17.8.101:30002/core/migration";
    private static final String CONTROLLER_DESTROY_URL = "http://172.17.8.101:30002/core/destroy";

    private static boolean isLeftRightMigrated = false;
    private static boolean isRightLeftMigrated = false;
    private static boolean isDestoyed = false;



    MigrationCopy(String name){
        threadName = name;
    }

    public void run() {
        String curServiceName = System.getenv("SERVICE_LABEL");
        String curNode = System.getenv("CUR_NODE");
        String type = "honda";
        //TODO: add type ENV to determine car's type
        if(curServiceName.substring(0, 1).equals("h")){
            type = "honda";
        }else if(curServiceName.substring(0, 1).equals("t")){
            type = "toyota";
        }

        while(true) {
            //TODO: develop your own migration algorithm
            //oldMigrateLogic(curServiceName, curNode, type);
            migrateLogic(curServiceName, curNode, type);
        }

    }
    private boolean lefttoRight(){
        if(ImoLocationApplication.locationHistoryData.size()>6) {
            Integer preLocation = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(5));
            Integer location = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(0));
            return preLocation < location ? true : false;
        }
        return false;
    }

    private void migrateLogic(String curServiceName, String curNode, String type){
        Integer location = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(0));
        if (location == 50) {
            if (lefttoRight()) {
                System.out.println(" Migrate " + curServiceName + " from edge1 to edge2");
                migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
            } else {
                System.out.println(" Migrate " + curServiceName + " from edge2 to edge1");
                migrate(curServiceName, type, EDGE_NODE2, EDGE_NODE1);
            }
        }
        if(location >=110){
            System.out.println(" Leaving the cloud, destory all DGs of " + curServiceName);
            destroy(curServiceName, curNode, type);
        }
    }

    private void oldMigrateLogic(String curServiceName, String curNode, String type){
        if(ImoLocationApplication.locationHistoryData.size()>6) {
            Integer preLocation = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(3));
            Integer location = Integer.valueOf(ImoLocationApplication.locationHistoryData.get(0));

            if(preLocation < location){ // from left to right
                if (location >= 40 && (!isLeftRightMigrated) && curNode.equals(EDGE_NODE1)) {
                    System.out.println(" Migrate to Edge Node 2");
                    migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
                    String migrateInfo = "Copy DG from " + EDGE_NODE1 + " to " + EDGE_NODE2;
                    isLeftRightMigrated = true;
                }
                if ((!isDestoyed) && ((location > 60 && curNode.equals(EDGE_NODE1))
                        || (location >= 100 && curNode.equals(EDGE_NODE2)))) {
                    // destroy it's self
                    System.out.println(" Destroy DGs on " + curNode);
                    destroy(curServiceName, type, curNode);
                    isDestoyed = true;
                }

            } else if(preLocation > location){ // from right to left
                if (location <= 60 && (!isRightLeftMigrated) && curNode.equals(EDGE_NODE2)) {
                    System.out.println(" Migrate to Edge Node 1");
                    migrate(curServiceName, type, EDGE_NODE2, EDGE_NODE1);
                    String migrateInfo = "Copy DG from " + EDGE_NODE2 + " to " + EDGE_NODE1;
                    isRightLeftMigrated = true;
                }
                if ((!isDestoyed) && ((location < 40 && curNode.equals(EDGE_NODE2))
                        || (location < 1 && curNode.equals(EDGE_NODE1)))) {
                    // destroy it's self
                    System.out.println(" Destroy DGs on " + curNode);
                    destroy(curServiceName, type, curNode);
                    isDestoyed = true;
                }
            }
        }
        try{
            Thread.sleep(1000);
        }catch (InterruptedException ie){
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
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> copyParamMap = new LinkedMultiValueMap<String, Object>();
        copyParamMap.add("name", name);
        copyParamMap.add("type", type);
        copyParamMap.add("srcNode", src);
        copyParamMap.add("dstNode", dst);

        boolean retry = true;
        int cnt = 5;
        while(retry && cnt>0){
            try {
                template.postForObject(CONTROLLER_COPY_URL, copyParamMap, String.class);
                System.out.println("Try to migrate DG form " + src + " to " + dst);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            DgCommonsApplication.delay(1);
            cnt--;
        }
        if(retry == true){
            Log log = new Log("location", name, 3);
            log.logUpload("Migrate DG of " + name + " from " + src + " to " + dst);
            System.out.println("Failed to migrate successfully!");
            return;
        }
        // Clean the runtime data, ready for others to use
        cleanRuntime();

        Log log = new Log("location", name, 3);
        log.logUpload("Migrate DG of " + name + " from " + src + " to " + dst);
    }
    //Clean the runtime information for other DGs
    //TODO: Send to other micro-service components to clean the runtime
    private void cleanRuntime(){
        ImoLocationApplication.locationHistoryData.clear();
        ImoLocationApplication.logQueue.clear();
    }

    //destroy the DGs on node
    private void destroy(String name, String type, String node){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("name", name);
        destroyParamMap.add("type", type);
        destroyParamMap.add("node", node);

        boolean retry = true;
        int cnt = 5;
        while(retry && cnt>0){
            try {
                template.postForObject(CONTROLLER_DESTROY_URL, destroyParamMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{ Thread.sleep(200); }catch (InterruptedException ie){ }
            cnt--;
        }

        Log log = new Log("location", name, 3);
        log.logUpload("Delete myself DG on " + node);
    }

}
