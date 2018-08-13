package com.dg.kj.imolocation;

import com.dg.kj.dgcommons.Log;
import com.dg.kj.dgcommons.LogThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

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
    public static String mongIP;

    // 启动的时候要注意，由于我们在controller中注入了RestTemplate，所以启动的时候需要实例化该类的一个实例
    @Autowired
    private RestTemplateBuilder builder;

    //使用RestTemplateBuilder来实例化RestTemplate对象，spring默认已经注入了RestTemplateBuilder实例
    @Bean
    public RestTemplate restTemplate() {
        return builder.build();
    }


    public static void main(String[] args) {
        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");
        mongIP = System.getenv("MONGODB_IP");

        locationHistoryData = new ArrayList<>();
        logQueue = new LinkedList<>();

        SpringApplication.run(ImoLocationApplication.class, args);

        logQueue.offer("Location service is starting...");

        LogThread logThread = new LogThread("location", curServiceName, logQueue, 3);
        logThread.start();

        MigrationCopy migrationCopyThread = new MigrationCopy(" Monitoring the location ");
        migrationCopyThread.start();
    }

}
