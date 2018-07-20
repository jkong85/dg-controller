package com.dg.kj.imolocation;

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
public class ImoLocationApplication {

    public static List<String> locationHistoryData = new ArrayList<>();
    public static Queue<String> logQueue = new LinkedList<>(); // Just for log
    public static String curServiceName;
    public static String curNode;

    public static void main(String[] args) {
        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");

        locationHistoryData = new ArrayList<>();
        logQueue = new LinkedList<>();

        SpringApplication.run(ImoLocationApplication.class, args);

        logQueue.offer(" location service is starting...");

        LogThread logThread = new LogThread("location", curServiceName, logQueue, 3);
        logThread.start();

        MigrationCopy migrationCopyThread = new MigrationCopy(" Monitoring the location ");
        migrationCopyThread.start();
    }
}
