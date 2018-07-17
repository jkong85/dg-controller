package com.dg.kj.imolocation;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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


    MigrationCopy(){

    }

    public void run() {
        while(true){
            // determine whether to migrate to other nodes
            //TODO: develop your own migration algorithm
            if(ImoLocationApplication.locationHistoryData.size()>0) {

                Integer location = ImoLocationApplication.locationHistoryData.get(0);
                String migrateInfo = null;

                String curServiceName = System.getenv("SERVICE_LABEL");
                String curNode = System.getenv("CUR_NODE");
                String type = "honda";

                if (location >= 40 && (!isMigrated)) {
                    migrate(curServiceName, type, EDGE_NODE1, EDGE_NODE2);
                    migrateInfo = "Copy DG from " + EDGE_NODE1 + " to " + EDGE_NODE2;
                    isMigrated = true;
                }
                if (!isDestroyed &&
                        ((location > 60 && curNode.equals(EDGE_NODE1))
                                || (location >= 100 && curNode.equals(EDGE_NODE2)))) {
                    // destroy it's self
                    destroy(curServiceName, type, curNode);
                    isDestroyed = true;
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
    private void destroy(String name, String type, String node){
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, Object> destroyParamMap = new LinkedMultiValueMap<String, Object>();
        destroyParamMap.add("serviceName", name);
        destroyParamMap.add("type", type);
        destroyParamMap.add("node", node);

        String result = template.postForObject(CONTROLLER_DESTROY_URL, destroyParamMap, String.class);

    }
}
