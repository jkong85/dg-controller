package com.dg.com.controllertest.controller;

import com.dg.com.controllertest.ControllerTestApplication;
import com.dg.com.controllertest.DgService;
import com.dg.com.controllertest.ImoDGs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class RegistrationController {
    private String K8sApiServer = "http://172.17.8.101:8080/";
    private String K8S_GET_PODS_API = "/api/v1/namespaces/default/pods?limit=500";
    private String K8S_GET_SERVICE_API = "api/v1/namespaces/default/services/";
    private String DOCKER_IMAGE_PREFIX = "jkong85/dg-imo-";
    private String VERSION = "0.1";

    private String EUREKA_CONTAINER_PORT = "8888";
    private String ZUUL_CONTAINER_PORT = "8889";
    private String TEST_CONTAINER_PORT = "9005";
    private String SPEED_CONTAINER_PORT = "9001";
    private String OIL_CONTAINER_PORT = "9002";
    private String LOCATION_CONTAINER_PORT = "9003";

    private String CORE_NODE = "node1";
    private String EDGE_NODE_1 = "node2";
    private String EDGE_NODE_2 = "node3";

    private String HONDA_TYPE = "honda";
    private String TOYOTA_TYPE = "toyota";

    private String LOG_PREFIX = "From Controller: ";
    private Set<String> copyRequstSet = new HashSet<>();

    @Autowired
    private ControllerTestApplication testApplication;

    @Autowired
    private LogController logController;

    @RequestMapping(value = "/register")
    public String register(@RequestParam String name,
                            @RequestParam String type,
                            @RequestParam String location) {
        //e.g., value = "Car1", we need generate "Car1-0" on core node, "Car1-1" service on edge node
        // To check whether it is already registered
        //TODO: change here
        String imoName = name;
        //TODO: change here based on the input
        String imoLocation = location;
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " received register request from " + imoName);
        //Map<String, ImoDGs>  dgInfoMap = testApplication.DGInfoMap;
        if( !testApplication.DGInfoMap.containsKey(imoName)){
            ImoDGs newImoDgs = new ImoDGs(imoName);
            String mainIMODG = imoName + "-1";
            DgService newDgService = createIMODG(mainIMODG, CORE_NODE, type);
            ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + "create DGs on core cloud node for " + imoName);
            if(newDgService == null){
                ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, "Creating the DG is failed");
                return "Cannot create DGs on core node for this car!";
            }
            newImoDgs.coreDG = newDgService;
            testApplication.DGInfoMap.put(imoName, newImoDgs);
        }
        // determine the location of the IMO
        String edgeLocation = getLocation(imoLocation);
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + "determine " + imoName + " is in edge node: " + edgeLocation);
        //Check whether there is a DG of this IMO  on this edge node
        boolean isExisted = false;
        for(DgService curDgService : testApplication.DGInfoMap.get(imoName).edgeDGs){
            if(curDgService.node.equals(edgeLocation)){
                isExisted = true;
                ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " DG is existed, no need to create a new one for " + imoName);
                System.out.println("There is one DG existed. No need to create a new one!");
                break;
            }
        }
        if(!isExisted){// create a new one on edge node
            try{
                Thread.sleep(500);
            }catch (InterruptedException ex){
                System.out.println(ex.toString());
            }
            System.out.println("Create a new DG on edge node : " + edgeLocation);
            ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " create a new DG on edge node: " + edgeLocation + " for " + imoName);
            Integer edgeIndex = testApplication.DGInfoMap.get(imoName).indexPool.pop();
            String edgeImoDg = imoName + "-" + Integer.toString(edgeIndex);
            DgService newDgService  = createIMODG(edgeImoDg, edgeLocation, type) ;
            if(newDgService == null){
                System.out.println("Cannot create DG for " + imoName + " on edge node : " + edgeLocation);
            }else {
                testApplication.DGInfoMap.get(imoName).edgeDGs.add(newDgService);
            }
        }
        // Find ou the IP:port of the DGs created on Core and Edge nodes
        String dstIPPort = testApplication.DGInfoMap.get(name).getAllDgIpPort();
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + "new DGs on Core and Edge nodes are created suuccessfully, \n       Access address:  " + dstIPPort);
        return dstIPPort;
    }

    // TODO: I use copyRequestSet to avoid the duplicate copy cmd, A better way is to use a work queue
    // TODO: copy/migration/register : put all command in a work queue, and multi-thread to handle the queue. It also can avoid the time-out HTTP response
    @RequestMapping(value = "/copy")
    public String copy(@RequestParam String name,
                       @RequestParam String type,
                       @RequestParam String srcNode,
                       @RequestParam String dstNode) {
        String hash = name + type + srcNode + dstNode;
        if(copyRequstSet.contains(hash)){
            return " Duplicate copy command, ignore it!";
        }
        copyRequstSet.add(hash);
        String source = srcNode;
        String destination = dstNode;
        String imoName = trimLastOne(name, "-");
        System.out.println("Receive copy cmd from " + srcNode + " to : " + dstNode + " for IMO: " + imoName);
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " Copy request from " + srcNode + " to "  + dstNode + " for " + imoName);

        if(source.equals(destination)){
            return "New destination of DG is the same with the source, no need to copy it!";
        }
        //TODO: need to valid the srcNode and dstNode
        boolean isExisted = false;
        if(!testApplication.DGInfoMap.containsKey(imoName)){
            return "Cannot find the DGs : " + imoName;
        }
        for(DgService curDgService : testApplication.DGInfoMap.get(imoName).edgeDGs){
            if(curDgService.node.equals(destination)){
                isExisted = true;
                break;
            }
        }
        if(!isExisted){// create a new one on edge node
            System.out.println("Create a new one on Node : " + destination);
            Integer edgeIndex = testApplication.DGInfoMap.get(imoName).indexPool.pop();
            String edgeImoDg = imoName + "-" + Integer.toString(edgeIndex);
            DgService newDgService  = createIMODG(edgeImoDg, destination, type) ;
            if(newDgService == null){
                System.out.println("Cannot create DG for " + imoName + " on edge node : " + destination);
            }else {
                ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " create a new DG on " + destination);
                testApplication.DGInfoMap.get(imoName).edgeDGs.add(newDgService);
            }
        }

        copyRequstSet.remove(hash);

        return "DG of " + imoName + " is copied to " + destination;
    }

    @RequestMapping(value = "/destroy")
    public String destroy(@RequestParam String serviceName,
                          @RequestParam String type,
                          @RequestParam String node){
        System.out.println("Get the destroy cmd from " + serviceName + " on node : " + node);
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " delete request from " + serviceName + " on node : " + node);
        List<String> deployList = new ArrayList<>();
        //basis components
        deployList.add("eureka");
        deployList.add("zuul");
        if(type.equals(HONDA_TYPE)){
            deployList.add("speed");
            deployList.add("location");
        }else if(type.equals(TOYOTA_TYPE)){
            deployList.add("oil");
            deployList.add("location");
        }
        Map<String, String> deployPodMap = new HashMap<>();
        findPodOfDeploymentMap(serviceName, deployList, deployPodMap);
        String urlPodPrefix = K8sApiServer + "api/v1/namespaces/default/pods/";
        String urlRCPrefix = K8sApiServer + "apis/extensions/v1beta1/namespaces/default/replicasets/";
        String urlDeployPrefix = K8sApiServer + "apis/extensions/v1beta1/namespaces/default/deployments/";
        // delete the deployment one by one
        for(String deployment : deployList){
            String deployname = serviceName + "-" + deployment;
            System.out.println("Destroy the DG " + deployment);
            System.out.println(deployment + " URL is : " + urlDeployPrefix+ deployname);
            httpDelete(urlDeployPrefix + deployname);
            String podName = deployPodMap.get(deployname);
            System.out.println(deployment + " RC URL is : " + urlRCPrefix + trimLastOne(podName, "-"));
            httpDelete(urlRCPrefix + trimLastOne(podName, "-"));
            System.out.println(deployment + " Pod URL is : " + urlPodPrefix + podName);
            httpDelete(urlPodPrefix + podName);
            ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " delete the micro-service: " + deployment);
        }
        //finally, delete the service
        String urlServcePrefix = K8sApiServer + "api/v1/namespaces/default/services/";
        System.out.println("Delete service: " + urlServcePrefix + serviceName);
        ControllerTestApplication.AddLog(ControllerTestApplication.CONTROLLER_LOG_NAME, LOG_PREFIX + " delete DG serice : " + serviceName);
        if(httpDelete(urlServcePrefix + serviceName)){
            Integer port = testApplication.ServicePortMap.get(serviceName);
            System.out.println("Release the port : " + port.toString());
            testApplication.nodePortsPool.push(port);
            //delete the DG info
            String imoName = trimLastOne(serviceName, "-");
            testApplication.DGInfoMap.get(imoName).indexPool.push(Integer.valueOf(getLastOne(serviceName, "-")));
            System.out.println("Release the index : " + getLastOne(serviceName, "-"));
            System.out.println("IMO name is: " + imoName);
            for(int i=0; i<testApplication.DGInfoMap.get(imoName).edgeDGs.size(); i++) {
                DgService curService = testApplication.DGInfoMap.get(imoName).edgeDGs.get(i);
                if(curService.name.equals(serviceName)){
                    testApplication.DGInfoMap.get(imoName).edgeDGs.remove(i);
                    System.out.println("remove the DG service : " + serviceName);
                    break;
                }
            }
        }

        //TODO: move it to a work queue
        // remove the log record of this DG
        try{
                Thread.sleep(500);
            }catch (InterruptedException ex){
                System.out.println(ex.toString());
            }
        ControllerTestApplication.DGCurLogMap.get(serviceName).clear();
        ControllerTestApplication.DGCurLogMap.remove(serviceName);
        return "Deleting the DG services";
    }

    private String trimLastOne(String src, String split){
        StringBuilder sb = new StringBuilder();
        String[] sub = src.split(split);
        sb.append(sub[0]);
        for(int i=1; i<sub.length-1; i++){
            sb.append(split);
            sb.append(sub[i]);
        }
        System.out.println("After trim the last seg : " + sb.toString());
        return sb.toString();
    }
    private String getLastOne(String src, String split){
        String[] sub = src.split(split);
        return sub[sub.length-1];
    }

    @RequestMapping(value = "/info")
    public String getInfo(@RequestParam String value){
        System.out.println("get the IP:port for core DG, and edge DG ");
        if(testApplication.DGInfoMap==null || !testApplication.DGInfoMap.containsKey(value)){
            return null;
        }
        return testApplication.DGInfoMap.get(value).getAllDgIpPort();
    }

    //TODO: override this based on the algorithms
    private String getLocation(String location){
        if(Integer.valueOf(location) < 60) {
            return EDGE_NODE_1;
        }else return EDGE_NODE_2;
    }

    private DgService createIMODG(String service_label, String node_selector, String type){
        //e.g., Car1-0-***, Car1-1-***
//        String service_label = "Car1-0";
//        String node_selector = "node1";
        System.out.println("Create an IMO DG: " + service_label);
        CreateEurekaDeployment(service_label, "localhost", node_selector);
        // Get eurkea ip after it is started
        String eureka_prefix = "eureka";
        String eureka_deploy_name =  service_label + "-" + eureka_prefix;
        // Wait for 2 min
        int wait = 120;
        String ip = null;
        while(wait-- > 0){
            System.out.println("Wait for Eureka starting ...");
            ip = getDeploymentIPaddress(eureka_deploy_name);
            if(isValidIP(ip)){
                break;
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException ex){
                System.out.println(ex.toString());
            }
        }
        if(!isValidIP(ip)){
            System.out.println("Eureka cannot start successfully!");
            return null;
        }

        String eureka_ip = getDeploymentIPaddress(eureka_deploy_name);
        System.out.println("Eureka server IP address is: " + eureka_ip);

        // Different type of Car will run different services
        if(type.equals(HONDA_TYPE)){
            CreateSpeedDeployment(service_label, eureka_ip, node_selector);
            CreateLocationDeployment(service_label, eureka_ip, node_selector);
        }else if (type.equals(TOYOTA_TYPE)){
            CreateLocationDeployment(service_label, eureka_ip, node_selector);
            CreateOilDeployment(service_label, eureka_ip, node_selector);
        }else{
            System.out.println("Not a correct car type!");
        }

        try{
            Thread.sleep(1000);
        }catch (InterruptedException ex){
            System.out.println(ex.toString());
        }
        CreateZuulDeployment(service_label, eureka_ip, node_selector);

        // Get the node port form the node po
        Integer nodePort_eureka = getIMONodePort();
        testApplication.ServicePortMap.put(service_label, nodePort_eureka);
        Integer nodePort_zuul = nodePort_eureka+1;
        CreateIMOService(service_label, nodePort_eureka.toString(), nodePort_zuul.toString());

        String nodeIP = testApplication.nodeIpMap.get(node_selector);

        return new DgService(service_label, node_selector, nodeIP, nodePort_eureka, nodePort_zuul);
    }

    private Integer getIMONodePort(){
        return testApplication.nodePortsPool.pop();
    }


    private String CreateSpeedDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "speed";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = SPEED_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return "Speed service is starting...\n";
    }

    private String CreateOilDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "oil";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = OIL_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return "Oil service is starting...\n";
    }
    private String CreateLocationDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "location";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = LOCATION_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return "Loaction service is starting...\n";
    }


    private String CreateTestDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "test";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = TEST_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return "Test service is starting...\n";
    }

    private String CreateEurekaDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "eureka";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = EUREKA_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);

        return "Eureka server is starting... \n";
    }

    private String CreateZuulDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "zuul";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = ZUUL_CONTAINER_PORT;

        CreateDeployment(K8sApiServer, deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return "API gateway Zuul is starting... \n";
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
        System.out.println("Start to create deployment : " + deploy_name);
        String urlDeployment = URLApiServer+ "apis/apps/v1/namespaces/default/deployments";

        String body = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"name\":\"" +
                deploy_name +
                "\",\"namespace\":\"default\"},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"app\":\"" +
                service_label +
                "\"}},\"template\":{\"metadata\":{\"labels\":{\"app\":\"" +
                service_label +
                "\"}},\"spec\":{\"containers\":[{\"env\":[{\"name\":\"EUREKA_SERVER_IP\",\"value\":\"" +
                eureka_ip +
                "\"},{\"name\":\"SERVICE_LABEL\",\"value\":\"" +
                service_label +
                "\"},{\"name\":\"CUR_NODE\",\"value\":\"" +
                node_selector +
                "\"}],\"image\":\"" +
                container_images +
                "\",\"name\":\"" +
                container_name +
                "\",\"ports\":[{\"containerPort\":" +
                container_port +
                "}]}],\"nodeSelector\":{\"kubernetes.io/hostname\":\"" +
                node_selector +
                "\"}}}}}";

        System.out.println("Create deployment HTTP body: " + body);
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

        System.out.println("Create Service HTTP body: " + body);

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

    private static boolean httpDelete(String urlService){
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.delete(urlService);
            System.out.println("Successful delete : " + urlService);
            return true;
        }catch(HttpClientErrorException he){
            System.out.println("Delete Exeception: " + urlService + " ==> " + he.toString());
            return false;
        }
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

    private boolean isValidIP(String ip){
        System.out.println("valid IP: " + ip);
        if(ip==null || ip.length()<7){
            return false;
        }
        // REGX is a better way
        String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
//            System.out.println("IP is: " + matcher.group());
            return true;
        }
        return false;
    }
    //ServiceName is the Service in K8S domain
    //e.g., name_deploy = "controller-eureka", or "car1-1-eureka"

    //TODO: maintain the global mapping information
    private String getDeploymentIPaddressFromMap(String name_deploy){
        Map<String, String> deployPodMap = new HashMap<>();
        Map<String, String> podIPaddressMap = new HashMap<>();
//        findDeployPodIPaddressMap(deployPodMap, podIPaddressMap);
//        return podIPaddressMap.get(deployPodMap.get(name_deploy));
        return null;
    }
    private void findPodOfDeploymentMap(String service, List<String> deploy, Map<String, String> depoyPodMap) {
        String urlGetPods = K8sApiServer + K8S_GET_PODS_API;
        String response = httpGet(urlGetPods);
        String[] pods_str_array = response.replace("\"", "").replace("{", "").split("metadata:");
//        System.out.println("All pods info: " + response);

        String name_start = "name:";
        //String name_deploy = "controller-eureka";
        String name_end = ",generateName";

        String podIP_start = "podIP:";
        String podIP_end = ",startTime";

        Matcher matcher;
        System.out.println("Find the mapping between Deployment and Pods");

        for (String dep : deploy) {
            String name_deploy = service + "-" + dep;
            Pattern pattern_name = Pattern.compile(name_start + name_deploy + ".+?" + name_end);
            for (int i = 0; i < pods_str_array.length; i++) {
                //System.out.println("==================================");
                //System.out.println(pods_str_array[i]);
                matcher = pattern_name.matcher(pods_str_array[i]);
                if (matcher.find()) {
                    String nameResult = matcher.group();
                    depoyPodMap.put(name_deploy, nameResult.substring(name_start.length(), nameResult.length() - name_end.length()));
                    System.out.println(name_deploy + " : " + depoyPodMap.get(name_deploy));
                }
            }
        }
    }

    private String getDeploymentIPaddress(String name_deploy){
        //String urlGetPods = K8sApiServer + "/api/v1/namespaces/default/pods?limit=500";
        String urlGetPods = K8sApiServer + K8S_GET_PODS_API;
        String response = httpGet(urlGetPods);
        return getDeploymentIPbyRegx(response, name_deploy);
    }

    //e.g., name_deploy = "controller-eureka", or "car1-1-eureka"
    private String getDeploymentIPbyRegx(String response, String name_deploy){
        String[] pods_str_array = response.replace("\"", "").replace("{", "").split("metadata:");
//        System.out.println("All pods info: " + response);

        String name_start = "name:";
        //String name_deploy = "controller-eureka";
        String name_end = ",generateName";

        String podIP_start = "podIP:";
        String podIP_end = ",startTime";

        Matcher matcher;
        Pattern pattern_name = Pattern.compile(name_start + name_deploy + ".+?" + name_end);
        Pattern pattern_podIP = Pattern.compile(podIP_start + ".+?" + podIP_end);
        for(int i=0; i<pods_str_array.length; i++){
//            System.out.println("==================================");
//            System.out.println(pods_str_array[i]);
            matcher = pattern_name.matcher(pods_str_array[i]);
            if(matcher.find()){
                String nameResult = matcher.group();
                System.out.println("Pod name is : " + nameResult.substring(name_start.length(), nameResult.length() - name_end.length()));
                matcher = pattern_podIP.matcher(pods_str_array[i]);
                if(matcher.find()){
                    String ipResult = matcher.group();
                    String podIP = ipResult.substring(podIP_start.length(), ipResult.length() - podIP_end.length());
                    System.out.println("Pod IP is : " +podIP);
                    return podIP;
                }
            }
        }
        return null;
    }


    private String getServiceClusterIP(String serviceName){
        //https://172.17.8.101:6443/api/v1/namespaces/default/services/serviceName
        //String url = K8sApiServer + "api/v1/namespaces/default/services/" + serviceName;
        String url = K8sApiServer + K8S_GET_SERVICE_API + serviceName;

        String response = httpGet(url);

        return "0.0.0.0";
    }
}
