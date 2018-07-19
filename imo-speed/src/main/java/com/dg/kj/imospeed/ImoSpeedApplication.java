package com.dg.kj.imospeed;

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
public class ImoSpeedApplication {
    public static String type = "honda";
    public static List<String> speedHistoryData = new ArrayList<>();
    public static Queue<String> logQueue = new LinkedList<>(); // Just for log
    public static String curServiceName = null;
    public static String curNode = null;

    public static void main(String[] args) {
        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");
        speedHistoryData = new ArrayList<>();
        logQueue = new LinkedList<>();

        SpringApplication.run(ImoSpeedApplication.class, args);

        logQueue.offer(" speed service is up");

        LogThread logThread = new LogThread("speed", curServiceName, logQueue, 3);
        logThread.start();
    }
}
