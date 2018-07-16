package com.dg.kj.imooil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class ImoOilApplication {

    public static List<Integer> oilHistoryData = new ArrayList<>();
    public static void main(String[] args) {
        oilHistoryData = new ArrayList<>();
        SpringApplication.run(ImoOilApplication.class, args);
    }
}
