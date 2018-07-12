package com.dg.kj.controllereureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ControllerEurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerEurekaApplication.class, args);
    }
}
