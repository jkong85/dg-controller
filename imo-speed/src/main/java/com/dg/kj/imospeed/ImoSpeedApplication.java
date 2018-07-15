package com.dg.kj.imospeed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class ImoSpeedApplication {
    public static List<Integer> speedHistoryData = new ArrayList<>();
    public static List<Integer> locationHistoryData = new ArrayList<>();
    public static void main(String[] args) {
        speedHistoryData = new ArrayList<>();
        locationHistoryData = new ArrayList<>();
        SpringApplication.run(ImoSpeedApplication.class, args);
    }
}
