package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import com.dg.com.controllercore.IMOs.Deployment;
import com.dg.kj.dgcommons.Http;
import org.springframework.web.client.HttpClientErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jkong on 7/25/18.
 */
public class ApiServerCmd {
    private String K8SApiServer = "http://172.17.8.101:8080/";
    private String URL_K8S_CREATE_SERVICE =  K8SApiServer + "api/v1/namespaces/default/services";
    private String URL_K8S_CREATE_DEPLOYMENT = K8SApiServer + "apis/apps/v1/namespaces/default/deployments";
    private String URL_K8S_GET_PODS_API = K8SApiServer + "api/v1/namespaces/default/pods?limit=500";
    private String URL_K8S_GET_SERVICE_API = K8SApiServer + "api/v1/namespaces/default/services/";
    private String DOCKER_IMAGE_PREFIX = "jkong85/dg-imo-";
    private String VERSION = "0.1";

    private String EUREKA_CONTAINER_PORT = "8888";
    private String ZUUL_CONTAINER_PORT = "8889";

    private String CORE_CONTAINER_PORT = "9004";
    private String TEST_CONTAINER_PORT = "9005";

    private String SPEED_CONTAINER_PORT = "9001";
    private String OIL_CONTAINER_PORT = "9002";
    private String LOCATION_CONTAINER_PORT = "9003";

    public ApiServerCmd(){

    }

    // Backupservice just create all deloyments with label selector, without encapsulated by K8s service
    // Label name:  DG-<node>-<type>-<index> ==> DG-core-honda-1
    // start Eureka, Zuul, and microservices of IMOs
    public BackupService createBackupService(BackupServiceRequest request, Integer index, Integer port_eureka, Integer port_zuul){
        String service_label = "DG-" + request.node + "-" + request.type + "-" + index.toString();
        return createBackupServiceDeployments(service_label, request.node, request.type);
    }
    public BackupService createBackupServiceDeployments(String service_label, String node_selector, String type){
        //e.g., Car1-0-***, Car1-1-***
//        String service_label = "Car1-0";
//        String node_selector = "node1";
        System.out.println("Create an IMO DG: " + service_label);
        BackupService backupService = new BackupService(service_label, type, service_label, node_selector);

        backupService.deploymentsList.add(CreateEurekaDeployment(service_label, "localhost", node_selector));
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
            backupService.deploymentsList.clear();
            return null;
        }

        String eureka_ip = getDeploymentIPaddress(eureka_deploy_name);
        System.out.println("Eureka server IP address is: " + eureka_ip);

        // Different type of Car will run different services
        if(type.equals(ControllerCoreApplication.HONDA)){
            backupService.deploymentsList.add(CreateSpeedDeployment(service_label, eureka_ip, node_selector));
            backupService.deploymentsList.add(CreateLocationDeployment(service_label, eureka_ip, node_selector));
        }else if (type.equals(ControllerCoreApplication.TOYOTA)){
            backupService.deploymentsList.add(CreateLocationDeployment(service_label, eureka_ip, node_selector));
            backupService.deploymentsList.add(CreateOilDeployment(service_label, eureka_ip, node_selector));
        }else{
            System.out.println("The type of car is not supported!");
        }

        try{
            Thread.sleep(1000);
        }catch (InterruptedException ex){
            System.out.println(ex.toString());
        }
        backupService.deploymentsList.add(CreateZuulDeployment(service_label, eureka_ip, node_selector));

        return backupService;
    }

    private Deployment CreateSpeedDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "speed";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = SPEED_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return new Deployment(deploy_name, node_selector);
    }

    private Deployment CreateOilDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "oil";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = OIL_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return new Deployment(deploy_name, node_selector);
    }
    private Deployment CreateLocationDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "location";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = LOCATION_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return new Deployment(deploy_name, node_selector);
    }


    private Deployment CreateTestDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "test";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = TEST_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return new Deployment(deploy_name, node_selector);
    }

    private Deployment CreateEurekaDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "eureka";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = EUREKA_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);

        return new Deployment(deploy_name, node_selector);
    }

    private Deployment CreateZuulDeployment(String service_label, String eureka_ip, String node_selector){
        String prefix = "zuul";
        String deploy_name =  service_label + "-" + prefix;
        String container_name = deploy_name;
        String container_images = DOCKER_IMAGE_PREFIX + prefix + ":" + VERSION;
        String container_port = ZUUL_CONTAINER_PORT;

        CreateDeployment(deploy_name, service_label,
                container_name, container_images, container_port, eureka_ip, node_selector);
        return new Deployment(deploy_name, node_selector);
    }


    public String CreateService( String name,
                                        String selector,
                                        String nodePort_eureka,
                                        String nodePort_zuul
    ) throws HttpClientErrorException{
        System.out.println("Start to create service : " + name);
        String urlService = URL_K8S_CREATE_SERVICE;
        String body = "{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"labels\":{\"app\":\"" +
                selector +
                "\"},\"name\":\"" +
                name +
                "\",\"namespace\":\"default\"},\"spec\":{\"ports\":[{\"name\":\"eureka-port\",\"nodePort\":" +
                nodePort_eureka +
                ",\"port\":80,\"targetPort\":" +
                EUREKA_CONTAINER_PORT +
                "},{\"name\":\"zuul-port\",\"nodePort\":" +
                nodePort_zuul +
                ",\"port\":8080,\"targetPort\":" +
                ZUUL_CONTAINER_PORT +
                "}],\"selector\":{\"app\":\"" +
                selector +
                "\"},\"type\":\"" + "NodePort" + "\"}}";

        System.out.println("Create Service HTTP body: " + body);

        String str = Http.httpPost(urlService, body);
        return str;
    }
    public String CreateDeployment( String deploy_name,
                                    String service_label,
                                    String container_name,
                                    String container_images,
                                    String container_port,
                                    String eureka_ip,
                                    String node_selector
    ) throws HttpClientErrorException {
        System.out.println("Start to create deployment : " + deploy_name);
        String urlDeployment = URL_K8S_CREATE_SERVICE;

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
                "}]}],\"nodeSelector\":{\"cloud.node\":\"" +
                node_selector +
                "\"}}}}}";

        System.out.println("Create deployment HTTP body: " + body);
        String str = Http.httpPost(urlDeployment, body);
        return str;
    }

    private String getDeploymentIPaddress(String name_deploy){
        String urlGetPods = URL_K8S_GET_PODS_API;
        String response = Http.httpGet(urlGetPods);
        return getDeploymentIPbyRegx(response, name_deploy);
    }

    //e.g., name_deploy = "controller-eureka", or "car1-1-eureka"
    private String getDeploymentIPbyRegx(String response, String name_deploy){
        String[] pods_str_array = response.replace("\"", "").replace("{", "").split("metadata:");

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
    private boolean isValidIP(String ip){
        if(ip==null || ip.length()<7){
            return false;
        }
        // REGX is a better way
        String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

}