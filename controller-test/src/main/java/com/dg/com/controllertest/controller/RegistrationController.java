package com.dg.com.controllertest.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class RegistrationController {
    private String K8sApiServer = "http://172.17.8.101:8080/";
    private String VERSION = "0.1";
    private String DOCKER_IMAGE_PREFIX = "jkong85/dg-imo-";
    private String EUREKA_CONTAINER_PORT = "8888";
    private String ZUUL_CONTAINER_PORT = "8889";
    private String TEST_CONTAINER_PORT = "9005";
    private String SPEED_CONTAINER_PORT = "9001";
    private String OIL_CONTAINER_PORT = "9002";

    private static Map<String, String> DGInformation = new HashMap<>();
    private static Set<Integer> setNodePorts = new HashSet<>();
    @RequestMapping(value = "/registration")
    public String create(@RequestParam String value){

        String service_label = value;
        String node_selector = "node1";
        CreateEurekaDeployment(service_label, "localhost", node_selector);
        String eureka_ip = getDeploymentIPaddress(service_label);
        CreateTestDeployment(service_label, eureka_ip, node_selector);
        CreateZuulDeployment(service_label, eureka_ip, node_selector);

        Integer nodePort_eureka = getIMONodePort();
        Integer nodePort_zuul = nodePort_eureka+1;
        CreateIMOService(service_label, nodePort_eureka.toString(), nodePort_zuul.toString());

        //store the information of the DG
        String service_ip = getServiceIPaddress(service_label);
        DGInformation.put(service_label, eureka_ip);

        return "DG is created successfully!";
        //return "Create Pod: " + value;
    }

    private Integer getIMONodePort(){
        return 30001;
    }
    //ServiceName is the Service in K8S domain
    private String getDeploymentIPaddress(String serviceName){

        return "localhost";
    }

    private String getServiceIPaddress(String serviceName){

        return "localhost";
    }
    private String CreateTestDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "test";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + VERSION;
        String container_port = TEST_CONTAINER_PORT;

        return CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
    }

    private String CreateEurekaDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "eureka";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + VERSION;
        String container_port = EUREKA_CONTAINER_PORT;

        return CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
    }

    private String CreateZuulDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "zuul";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + VERSION;
        String container_port = ZUUL_CONTAINER_PORT;

        return CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
    }

    private String CreateDeployment(String URLApiServer,
                                    String deploy_name,
                                    String service_label,
                                    String container_name,
                                    String container_images,
                                    String container_port,
                                    String eureka_ip,
                                    String node_selector
                                    ) throws HttpClientErrorException {
        System.out.println("Start to create pod!");
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        String urlDeployment = URLApiServer+ "apis/apps/v1/namespaces/default/deployments";

        String body = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"name\":\"" +
                deploy_name +
                "\",\"namespace\":\"default\"},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"app\":\"" +
                service_label +
                "\"}},\"template\":{\"metadata\":{\"labels\":{\"app\":\"" +
                service_label +
                "\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"EUREKA_SERVER_IP\",\"value\":\"" +
                eureka_ip +
                "\"}],\"image\":\"" +
                container_images +
                "\",\"name\":\"" +
                container_name +
                "\",\"ports\":[{\"containerPort\":" +
                container_port +
                "}]}],\"nodeSelector\":{\"kubernetes.io/hostname\":\"" +
                node_selector +
                "\"}}}}}";
        //String body = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"name\":\"controller-test\",\"namespace\":\"default\"},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"app\":\"controller\"}},\"template\":{\"metadata\":{\"labels\":{\"app\":\"controller\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"EUREKA_SERVER_IP\",\"value\":\"10.1.0.78\"}],\"image\":\"jkong85/dg-controller-test:0.2\",\"name\":\"controller-test\",\"ports\":[{\"containerPort\":9005}]}],\"nodeSelector\":{\"kubernetes.io/hostname\":\"node1\"}}}}}";
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

    private String CreateIMOService(String service_label,
                                    String nodePort_eureka,
                                    String nodePort_zuul ){
        return CreateService(K8sApiServer, service_label, nodePort_eureka, nodePort_zuul);
    }
    private String CreateService(String URLApiServer,
                                 String service_label,
                                 String nodePort_eureka,
                                 String nodePort_zuul
                                ) throws HttpClientErrorException{
        System.out.println("Start to create service : " + service_label);
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";
        String urlService = URLApiServer+ "api/v1/namespaces/default/services";

        String body = "{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"labels\":{\"app\":\"" +
                service_label +
                "\"},\"name\":\"" +
                service_label +
                "\",\"namespace\":\"default\"},\"spec\":{\"ports\":[{\"name\":\"eureka-port\",\"nodePort\":" +
                nodePort_eureka +
                ",\"port\":80,\"targetPort\":" +
                EUREKA_CONTAINER_PORT +
                "},{\"name\":\"zuul-port\",\"nodePort\":" +
                nodePort_zuul +
                ",\"port\":8080,\"targetPort\":" +
                ZUUL_CONTAINER_PORT +
                "}],\"selector\":{\"app\":\"" +
                service_label +
                "\"},\"type\":\"" + "NodePort" + "\"}}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("headers: " + headers.toString());
        System.out.println("body : " + body);
        String str = restTemplate.postForObject(urlService, httpEntity, String.class);
        System.out.println(str);
        System.out.println("end of creating pod!");
        return str;
    }
}
