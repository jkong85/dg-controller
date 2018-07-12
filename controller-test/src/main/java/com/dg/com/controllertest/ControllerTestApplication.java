package com.dg.com.controllertest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerTestApplication.class, args);
    }
}
