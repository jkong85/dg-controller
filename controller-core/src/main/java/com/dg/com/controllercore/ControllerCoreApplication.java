package com.dg.com.controllercore;

import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.BackupServiceCheckThread;
import com.dg.com.controllercore.Tasks.BackupServiceMaintainThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerCoreApplication {
    public static final String CORE_NODE = "core";
    public static final String EDGE_NODE_1 = "edge1";
    public static final String EDGE_NODE_2 = "edge2";
    public static final String[] NODE_LIST = {CORE_NODE, EDGE_NODE_1, EDGE_NODE_2};

    public static final Map<String, String>  nodeIPMap = new HashMap<>();

    public static final String FORD = "ford";
    public static final String HONDA = "honda";
    public static final String TOYOTA = "toyota";
    public static final String[] IMO_TYPE = {FORD, HONDA, TOYOTA};

    public static final Integer BACKUP_LIMIT = 2;

    public static Stack<Integer> bkServiceIndexPoolStack;

    public static Map<String, String> nodeIpMap = new HashMap<>();
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();

    // Map<node, Map<car_type, Stack<BackupService>>>
    public static Map<String, Map<String, Stack<BackupService>>> bkServicePoolMap;
    public static Queue<BackupServiceRequest> bkServiceRequestQueue;

    // Global infor of car's DGS
    public static Map<String, IMO> IMOMap = new HashMap<>();

    public static void main(String[] args) {
        Initialize();

        SpringApplication.run(ControllerCoreApplication.class, args);

        BackupServiceMaintainThread backupServiceMaintainThread = new BackupServiceMaintainThread();
        backupServiceMaintainThread.start();
        BackupServiceCheckThread backupServiceCheckThread = new BackupServiceCheckThread();
        backupServiceCheckThread.start();
    }
    private static void Initialize(){
        bkServiceRequestQueue = new LinkedList<>();
        bkServiceIndexPoolStack = new Stack<>();
        nodePortsPool = new Stack<>();

        for(int i=1000; i>0; i--){
            bkServiceIndexPoolStack.push(i);
        }
        for(int i=32000; i>30004; i-=2){
            nodePortsPool.push(i);
        }

        for(String node: NODE_LIST){
            for(String type: IMO_TYPE){
                Stack<BackupService> bkServiceStack = new Stack<>();
                Map<String, Stack<BackupService>> car_service_map = new HashMap<>();
                car_service_map.put(type, bkServiceStack);
                bkServicePoolMap.put(node, car_service_map);
                for(int i=0; i<BACKUP_LIMIT; i++){
                    bkServiceRequestQueue.offer(new BackupServiceRequest(node, type));
                }
            }
        }
        nodeIpMap = new HashMap<>();

        nodeIpMap.put(CORE_NODE, "172.17.8.101");
        nodeIpMap.put(EDGE_NODE_1, "172.17.8.102");
        nodeIpMap.put(EDGE_NODE_2, "172.17.8.103");


        IMOMap = new HashMap<>();

    }
    public static void AddLog(String sender, String log){

    }
}