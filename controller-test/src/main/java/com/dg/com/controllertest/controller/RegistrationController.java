package com.dg.com.controllertest.controller;

import com.dg.com.controllertest.ControllerTestApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@RestController
public class RegistrationController {
    @Autowired
    private ControllerTestApplication testApplication;

    private String K8sApiServer = "http://172.17.8.101:8080/";
    private String VERSION = "0.1";
    private String DOCKER_IMAGE_PREFIX = "jkong85/dg-imo-";
    private String EUREKA_CONTAINER_PORT = "8888";
    private String ZUUL_CONTAINER_PORT = "8889";
    private String TEST_CONTAINER_PORT = "9005";
    private String SPEED_CONTAINER_PORT = "9001";
    private String OIL_CONTAINER_PORT = "9002";

    @RequestMapping(value = "/registration")
    public String create(@RequestParam String value){
        String service_label = value;
        //TODO: determine node based on the location
        String node_selector = "node1";

        CreateEurekaDeployment(service_label, "localhost", node_selector);
        // Get eurkea ip after it is started
        String eureka_prefix = "eureka";
        String eureka_deploy_name =  service_label + "-" + eureka_prefix;
        while(getDeploymentIPaddress(eureka_deploy_name) == null){
            try{
               Thread.sleep(5000);
            }catch (InterruptedException ex){
                System.out.println(ex.toString());
            }
        }
        try{
            Thread.sleep(1000);
        }catch (InterruptedException ex){
            System.out.println(ex.toString());
        }
        String eureka_ip = getDeploymentIPaddress(eureka_deploy_name);
        System.out.println("Eureka server IP address is: " + eureka_ip);

        /*
        CreateSpeedDeployment(service_label, eureka_ip, node_selector);
        try{
            Thread.sleep(5000);
        }catch (InterruptedException ex){
            System.out.println(ex.toString());
        }
        CreateZuulDeployment(service_label, eureka_ip, node_selector);

        // Get the node port form the node po
        Integer nodePort_eureka = getIMONodePort();
        Integer nodePort_zuul = nodePort_eureka+1;
        CreateIMOService(service_label, nodePort_eureka.toString(), nodePort_zuul.toString());

        //store the information of the DG
        String serviceClusterIP = getServiceClusterIP(service_label);
        testApplication.DGInformation.put(service_label, eureka_ip);
        */

        return "DG is created successfully!";
        //return "Create Pod: " + value;
    }

    private Integer getIMONodePort(){
        return testApplication.nodePortsPool.pop();
    }
    //ServiceName is the Service in K8S domain
    private String getDeploymentIPaddress(String deployment){
        //based on 'deployment', we need first find the pod name
        //

        return null;
    }

    private String getServiceClusterIP(String serviceName){
        //https://172.17.8.101:6443/api/v1/namespaces/default/services/serviceName
        String url = K8sApiServer + "api/v1/namespaces/default/services/" + serviceName;
        String response = httpGet(url);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode spec = root.path("spec");
            String clusterIP = spec.get("clusterIP").toString();
            return clusterIP;
        }catch(IOException ex){
            System.out.println(ex.toString());
        }
        return "0.0.0.0";
    }
    private String CreateSpeedDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "speed";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + VERSION;
        String container_port = SPEED_CONTAINER_PORT;

        return CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
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
        String str = httpPost(urlDeployment, body);
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

        String str = httpPost(urlService, body);
        return str;
    }
    private static String httpGet(String urlService){
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        RestTemplate restTemplate = new RestTemplate();
        String str = restTemplate.getForObject(urlService, String.class);
        return str;

        //ResponseEntity<String> response = restTemplate.getForEntity(urlService, String.class);
        //return response.getBody();

    }
    private static String httpPost(String urlService, String body) throws HttpClientErrorException{
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        String str = restTemplate.postForObject(urlService, httpEntity, String.class);
        return str;
    }
}
