package com.dg.com.controllertest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerTestApplication {
    public static Map<String, String> nodeIpMap = new HashMap<>();
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();
    // Maintain All DG information
    public static Map<String, ImoDGs>  DGInfoMap = new HashMap<>();

    public static Map<String, String> DeployPodMap = new HashMap<>();
    public static Map<String, String> PodIPaddressMap = new HashMap<>();

    public static void main(String[] args) {
        Initialize();
        SpringApplication.run(ControllerTestApplication.class, args);
    }
    private static void Initialize(){
        DGInfoMap = new HashMap<>();

        nodeIpMap = new HashMap<>();
        nodeIpMap.put("node1", "172.17.8.101");
        nodeIpMap.put("node2", "172.17.8.102");
        nodeIpMap.put("node3", "172.17.8.103");

        nodePortsPool = new Stack<>();
        // initialize node ports pool
        for(int i=32000; i>30004; i-=2){
            nodePortsPool.push(i);
        }

        DeployPodMap = new HashMap<>();
        PodIPaddressMap = new HashMap<>();
    }
}
