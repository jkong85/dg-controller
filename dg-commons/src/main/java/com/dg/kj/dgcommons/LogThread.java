package com.dg.kj.dgcommons;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedList;
import java.util.Queue;

public class LogThread implements Runnable{
    private Thread t;
    private String type;
    private String sender;
    private String url;
    private int cnt = 3;

    private static final String URL_CONTROLLER_LOG = "http://172.17.8.101:30002/test/logwrite";

    public static Queue<String> logQueue = new LinkedList<String>(); // Just for log

    public LogThread(String type, String sender, Queue<String> logQueue, int cnt) {
        this.type = type;
        this.sender = sender;
        this.logQueue = logQueue;
        this.url = URL_CONTROLLER_LOG;
        this.cnt = cnt;
    }

    public void run() {
        System.out.println("Running thread " + type);
        while(true) {
            if(!logQueue.isEmpty()) {
                String value = logQueue.poll();
                String log = ">>"+ sender + " " + type + " data : "  + value;
                logUpload(log);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, type);
            t.start ();
        }
    }
    public void logUpload(String log){
        MultiValueMap<String, Object> logParamMap = new LinkedMultiValueMap<String, Object>();
        logParamMap.add("sender", sender);
        logParamMap.add("log", log);

        Http.httpPost(url, logParamMap, cnt);
    }
}
