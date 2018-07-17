package com.dg.kj.imolocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class ImoLocationApplication {

    public static List<Integer> locationHistoryData = new ArrayList<>();
    public static void main(String[] args) {
        locationHistoryData = new ArrayList<>();
        SpringApplication.run(ImoLocationApplication.class, args);

        MigrationCopy migrationCopyThread = new MigrationCopy(" Monitoring the location ");
        migrationCopyThread.start();
    }
}
