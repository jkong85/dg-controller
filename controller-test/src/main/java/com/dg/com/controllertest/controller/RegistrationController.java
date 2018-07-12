package com.dg.com.controllertest.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
public class RegistrationController {
    private String URLApiServer = "http://172.17.8.101:8080/";
    @RequestMapping(value = "/registration")
    public String create(@RequestParam String value){
        return CreateDeployment();
        //return "Create Pod: " + value;
    }

    private String CreateDeployment( ) throws HttpClientErrorException {
        System.out.println("Strat to create pod!");
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        String urlDeployment = URLApiServer+ "apis/apps/v1/namespaces/default/deployments";
        String body = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"name\":\"controller-test\",\"namespace\":\"default\"},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"app\":\"controller\"}},\"template\":{\"metadata\":{\"labels\":{\"app\":\"controller\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"EUREKA_SERVER_IP\",\"value\":\"10.1.0.78\"}],\"image\":\"jkong85/dg-controller-test:0.2\",\"name\":\"controller-test\",\"ports\":[{\"containerPort\":9005}]}],\"nodeSelector\":{\"kubernetes.io/hostname\":\"node1\"}}}}}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("headers: " + headers.toString());
        System.out.println("body : " + body);
        String str = restTemplate.postForObject(urlDeployment, httpEntity, String.class);
        System.out.println(str);
        System.out.println("end of creating pod!");
        return str;
    }
    private void CreateService(){

    }
    private String getPodIP(){
        String IP = "0.0.0.0";

        return IP;
    }
}
