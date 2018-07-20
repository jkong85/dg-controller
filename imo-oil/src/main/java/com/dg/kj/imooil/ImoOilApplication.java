package com.dg.kj.imooil;

import com.dg.kj.dgcommons.LogThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@SpringBootApplication
@EnableDiscoveryClient
public class ImoOilApplication {
    public static List<String> oilHistoryData = new ArrayList<>();
    public static Queue<String> logQueue = new LinkedList<>(); // Just for log
    public static String curServiceName = null;
    public static String curNode = null;

    public static void main(String[] args) {
        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");

        oilHistoryData = new ArrayList<>();
        logQueue = new LinkedList<>();

        SpringApplication.run(ImoOilApplication.class, args);

        logQueue.offer(" oil service is starting...");

        LogThread logThread = new LogThread("oil", curServiceName, logQueue, 3);
        logThread.start();
    }
}
