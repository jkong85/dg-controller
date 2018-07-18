package com.dg.kj.imolocation;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class LogThread implements Runnable{
    private Thread t;
    private String threadName;

    private static final String URL_CONTROLLER_LOG = "http://172.17.8.101:30002/test/logwrite";

    LogThread( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }

    public void run() {
        System.out.println("Running " + threadName);
        RestTemplate template = new RestTemplate();
        while(true) {
            if(ImoLocationApplication.logQueue.isEmpty()){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                }
            }
            MultiValueMap<String, Object> copyParamMap = new LinkedMultiValueMap<String, Object>();
            copyParamMap.add("sender", ImoLocationApplication.curServiceName);
            copyParamMap.add("log", Integer.toString(ImoLocationApplication.logQueue.poll()));

            boolean retry = true;
            int cnt = 3;
            while (retry && cnt-- > 0) {
                try {
                    String result = template.postForObject(URL_CONTROLLER_LOG, copyParamMap, String.class);
                    retry = false;
                } catch (RestClientException re) {
                    retry = true;
                    System.out.println(re);
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
