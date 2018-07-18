package com.dg.kj.imolocation;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class MigrationCopy implements Runnable {
    private Thread t;
    private String threadName;

     private static final String CORE_NODE = "node1";
    private static final String EDGE_NODE1 = "node2";
    private static final String EDGE_NODE2 = "node3";
    private static final String CONTROLLER_COPY_URL = "http://172.17.8.101:30002/test/copy";
    private static final String CONTROLLER_DESTROY_URL = "http://172.17.8.101:30002/test/destroy";
    private static boolean isMigrated = false;

    private static boolean isDestroyed = false;


    MigrationCopy(String name){
        threadName = name;
    }

    public void run() {
        String curServiceName = System.getenv("SERVICE_LABEL");
        String curNode = System.getenv("CUR_NODE");
        String type = "honda";
        boolean test = false;

        if(test) { // just of test
            int cnt = 20;
            while (cnt-- > 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                }
                System.out.println("Count down " + cnt);
            }
            System.out.println("CUR_NODE: " + curNode + " , curServiceName: " + curServiceName);
            if (curNode.equals(EDGE_NODE1)) {
                System.out.println("Copy DG from " + EDGE_NODE1 + " to " + EDGE_NODE2);
                migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
                isMigrated = true;
                System.out.println("Copy is done!");
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException ie) {
            }

            System.out.println(" Destroy DGs on " + curNode);
            if (curNode.equals(EDGE_NODE1)) {
                destroy(curServiceName, type, curNode);
                isDestroyed = true;
                System.out.println("Destroy is done!");
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException ie) {
            }
        }

        while(true){
            //TODO: develop your own migration algorithm
            if(ImoLocationApplication.locationHistoryData.size()>0) {
                Integer location = ImoLocationApplication.locationHistoryData.get(0);

                if (location >= 40 && (!isMigrated)) {
                    System.out.println(" Migrate to Edge Node 2");
                    migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
                    String migrateInfo = "Copy DG from " + EDGE_NODE1 + " to " + EDGE_NODE2;
                    isMigrated = true;
                }
                if (((location > 60 && curNode.equals(EDGE_NODE1))
                                || (location >= 100 && curNode.equals(EDGE_NODE2)))) {
                    // destroy it's self
                    System.out.println(" Destroy DGs on " + curNode);
                    destroy(curServiceName, type, curNode);
                }
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException ie){
            }
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
        while(retry && cnt-->0){
            System.out.println("Try " + Integer.toString(6-cnt) + " time to migrate the DGs of " + name);
            try {
                String result = template.postForObject(CONTROLLER_COPY_URL, copyParamMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException ie){
            }
        }
        if(retry == true){
            System.out.println("Cannot migrate successfully!");
        }
    }
    //destroy the DGs on node
    private void destroy(String name, String type, String node){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("serviceName", name);
        destroyParamMap.add("type", type);
        destroyParamMap.add("node", node);

        boolean retry = true;
        int cnt = 5;
        while(retry && cnt-->0){
            try {
                String result = template.postForObject(CONTROLLER_DESTROY_URL, destroyParamMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{
                Thread.sleep(200);
            }catch (InterruptedException ie){
            }
        }

    }
}
