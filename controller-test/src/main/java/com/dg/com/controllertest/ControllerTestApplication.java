package com.dg.com.controllertest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerTestApplication {
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();
    // Maintain All DG information
    public static Map<String, IMO>  DGInfoMap = new HashMap<>();

    public static List<Integer> speedData = new ArrayList<>();

    public static void main(String[] args) {
        DGInfoMap = new HashMap<>();
        nodePortsPool = new Stack<>();
        speedData = new ArrayList<>();
        // initialize node ports pool
        for(int i=32000; i>30004; i-=2){
            nodePortsPool.push(i);
        }
        SpringApplication.run(ControllerTestApplication.class, args);
    }

    private class Deployment{
        boolean isDeployed;
        String name;
        String podName;
        String podIP;
        public Deployment(String name, boolean isDeployed){
            this.name = name;
            this.isDeployed = isDeployed;
            this.podName = null;
            this.podIP = null;
        }
    }

    private class Service{
        Integer index;
        String name;
        String clusterIP;
        String nodeIP;
        String eureka_node_port;
        String zuul_node_port;
        Deployment eureka;
        Deployment zuul;
        Deployment test;
        Deployment speed;
        Deployment oil;
        public Service(String name, String eureka_node_port, String zuul_node_port){
            this.name = name;
            this.eureka_node_port = eureka_node_port;
            this.zuul_node_port = zuul_node_port;
            eureka = new Deployment(name + "-eureka", false);
            zuul = new Deployment(name + "-zuul", false);
            test = new Deployment(name + "-test", false);
            speed = new Deployment(name + "-speed", false);
            oil = new Deployment(name + "-oil", false);
        }
    }

    private class IMO{
        String name;
        Stack<Integer> index;
        List<Service> dg;
        public IMO(String name){
            this.name = name;
            index = new Stack<>();
            for(int i=10; i>-1; i--){
                index.push(i);
            }
            dg = new ArrayList<>();
        }
    }

}
