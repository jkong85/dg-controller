package com.dg.kj.imolocation;

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

    public static List<Integer> locationHistoryData = new ArrayList<>();
    public static Queue<Integer> logQueue = new LinkedList<>(); // Just for log
    public static String curServiceName;
    public static String curNode;
    public static void main(String[] args) {
        locationHistoryData = new ArrayList<>();

        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");

        SpringApplication.run(ImoLocationApplication.class, args);

        LogThread logThread = new LogThread("Upload log to controller");
        logThread.start();

        MigrationCopy migrationCopyThread = new MigrationCopy(" Monitoring the location ");
        migrationCopyThread.start();
    }
}
