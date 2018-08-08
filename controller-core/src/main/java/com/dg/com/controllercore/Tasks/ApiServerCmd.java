package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;
import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import com.dg.com.controllercore.IMOs.Deployment;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jkong on 7/25/18.
 */
public class ApiServerCmd {
    private static final Logger logger = LogManager.getLogger(ApiServerCmd.class);

    private String K8SApiServer = "http://172.17.8.101:8080/";
    private String URL_K8S_CREATE_SERVICE =  K8SApiServer + "api/v1/namespaces/default/services";
    private String URL_K8S_DELETE_SERVICE =  K8SApiServer + "api/v1/namespaces/default/services/";
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
    // Label name:  dg-<node>-<type>-<index> ==> dg-core-honda-1
    public BackupService createBackupService(BackupServiceRequest request, Integer index, Integer port_eureka, Integer port_zuul){
        String service_label = "dg-" + request.node + "-" + request.type + "-" + index.toString();
        return createBackupServiceDeployments(service_label, request.node, request.type);
    }
    public BackupService createBackupServiceDeployments(String service_label, String node_selector, String type){
        logger.info("Create an backupService and its deployments: " + service_label);
        BackupService backupService = new BackupService(service_label, type, service_label, node_selector);

        Deployment eurekaDeployment = new Deployment(service_label, node_selector);
        try {
            eurekaDeployment = CreateEurekaDeployment(service_label, "localhost", node_selector);
        }catch (HttpClientErrorException he){
            logger.warn("Cannot create eureka deployment successfully!");
            logger.warn(he.toString());
            return null;
        }
        backupService.deploymentsList.add(eurekaDeployment);
        String eureka_prefix = "eureka";
        String eureka_deploy_name =  service_label + "-" + eureka_prefix;
        // Wait for 2 min
        int wait = 120;
        String ip = null;
        while(wait-- > 0){
            logger.debug("Wait for Eureka starting ...");
            ip = getDeploymentIPaddress(eureka_deploy_name);
            if(isValidIP(ip)){
                break;
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException ex){
                logger.debug(ex.toString());
            }
        }
        if(!isValidIP(ip)){
            logger.debug("Eureka cannot start successfully !");
            backupService.deploymentsList.clear();
            return null;
        }

        String eureka_ip = getDeploymentIPaddress(eureka_deploy_name);
        logger.debug("Eureka service IP address : " + eureka_ip);

        // Different type of Car will run different services
        if(type.equals(ControllerCoreApplication.HONDA)){
            backupService.deploymentsList.add(CreateSpeedDeployment(service_label, eureka_ip, node_selector));
            backupService.deploymentsList.add(CreateLocationDeployment(service_label, eureka_ip, node_selector));
        }else if (type.equals(ControllerCoreApplication.TOYOTA)){
            backupService.deploymentsList.add(CreateLocationDeployment(service_label, eureka_ip, node_selector));
            backupService.deploymentsList.add(CreateOilDeployment(service_label, eureka_ip, node_selector));
        }else{
            logger.debug("Car type : " + type + " is not supported ! ");
        }

        try{
            Thread.sleep(1000);
        }catch (InterruptedException ex){
            logger.debug(ex.toString());
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

    private Deployment CreateEurekaDeployment(String service_label, String eureka_ip, String node_selector) throws HttpClientErrorException{
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
        logger.debug("Start to create service : " + name);
        String urlService = URL_K8S_CREATE_SERVICE;
        String body = "{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"labels\":{\"dg\":\"" +
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
                "}],\"selector\":{\"dg\":\"" +
                selector +
                "\"},\"type\":\"" + "NodePort" + "\"}}";

        logger.info("Create Service HTTP body: " + body);

        String str = Http.httpPost(urlService, body);
        return str;
    }

    public String deleteService(String serviceName, Integer port){
        logger.info("delete check service " + serviceName + " with port : " + port);
        logger.info("delete url is: " + URL_K8S_DELETE_SERVICE + serviceName);
        if(Http.httpDelete(URL_K8S_DELETE_SERVICE + serviceName)){
            ControllerCoreApplication.nodePortsPool.push(port);
        }
        return "Delete service successfully!";
    }


    public String CreateDeployment( String deploy_name,
                                    String service_label,
                                    String container_name,
                                    String container_images,
                                    String container_port,
                                    String eureka_ip,
                                    String node_selector
    ) throws HttpClientErrorException {
        logger.info("Create deployment by calling k8s API server: " + deploy_name);
        String urlDeployment = URL_K8S_CREATE_DEPLOYMENT;

        String body = "{\"apiVersion\":\"apps/v1\",\"kind\":\"Deployment\",\"metadata\":{\"name\":\"" +
                deploy_name +
                "\",\"namespace\":\"default\"},\"spec\":{\"replicas\":1,\"selector\":{\"matchLabels\":{\"dg\":\"" +
                service_label +
                "\"}},\"template\":{\"metadata\":{\"labels\":{\"dg\":\"" +
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

        logger.info("Deployment creation HTTP body: " + body);
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
            logger.trace(pods_str_array[i]);
            matcher = pattern_name.matcher(pods_str_array[i]);
            if(matcher.find()){
                String nameResult = matcher.group();
                logger.debug("Pod name is : " + nameResult.substring(name_start.length(), nameResult.length() - name_end.length()));
                matcher = pattern_podIP.matcher(pods_str_array[i]);
                if(matcher.find()){
                    String ipResult = matcher.group();
                    String podIP = ipResult.substring(podIP_start.length(), ipResult.length() - podIP_end.length());
                    logger.debug("Pod IP is : " +podIP);
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
