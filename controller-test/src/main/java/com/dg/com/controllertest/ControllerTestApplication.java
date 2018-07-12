package com.dg.com.controllertest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerTestApplication {

    public static Map<String, String> DGInformation = new HashMap<>();
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();

    public static List<Integer> speedData = new ArrayList<>();

    public static void main(String[] args) {
        DGInformation = new HashMap<>();
        nodePortsPool = new Stack<>();
        speedData = new ArrayList<>();
        // initialize node ports pool
        for(int i=30005; i<32000; i+=2){
            nodePortsPool.push(i);
        }
        SpringApplication.run(ControllerTestApplication.class, args);
    }
}
