package com.dg.com.controllercore;

import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.BkServiceCheckAvailNumberThread;
import com.dg.com.controllercore.Tasks.BkServiceCreateThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.util.*;

@SpringBootApplication
@EnableDiscoveryClient
public class ControllerCoreApplication {
    public static final String CORE_NODE = "core";
    public static final String EDGE_NODE_1 = "edge1";
    public static final String EDGE_NODE_2 = "edge2";
    public static final String[] NODE_LIST = {"core", "edge1", "edge2"};

    public static final Map<String, String>  nodeIPMap = new HashMap<>();

    public static final String FORD = "ford";
    public static final String HONDA = "honda";
    public static final String TOYOTA = "toyota";
    public static final String[] IMO_TYPE = {"ford", "honda", "toyota"};

    public static final Integer BACKUP_LIMIT = 1;
    public static final Integer BACKUP_INITIAL_LIMIT = 2;

    //Status of the backup service
    public static final Integer BK_SERVICE_STATUS_NOT_READY = 0;
    public static final Integer BK_SERVICE_STATUS_AVAILABLE = 1;
    public static final Integer BK_SERVICE_STATUS_USED = 2;

    public static Stack<Integer> bkServiceIndexPoolStack;

    public static Map<String, String> nodeIpMap = new HashMap<>();
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();

    // Map<node+car_type, List<BackupService>>>
    public static Map<String, List<BackupService>> bkServiceReadyPoolMap = new HashMap<>();
    public static Map<String, List<BackupService>> bkServiceNotReadyPoolMap = new HashMap<>();

    public static Queue<BackupServiceRequest> bkServiceRequestQueue;

    // Global info of car's DGS
    public static Map<String, IMO> IMOMap = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(ControllerCoreApplication.class);

    public static void main(String[] args) {
        Initialize();

        SpringApplication.run(ControllerCoreApplication.class, args);

        Integer wait = 2;
        while(wait-- > 0) {
            logger.info("Wait for" + wait.toString() + " seconds");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }

        BkServiceCreateThread bkServiceCreateThread = new BkServiceCreateThread();
        bkServiceCreateThread.start();

        BkServiceCheckAvailNumberThread bkServiceCheckAvailNumberThread = new BkServiceCheckAvailNumberThread();
        bkServiceCheckAvailNumberThread.start();

    }
    private static void Initialize(){
        logger.debug("Start initialize ...");
        bkServiceRequestQueue = new LinkedList<>();
        bkServiceIndexPoolStack = new Stack<>();
        bkServiceReadyPoolMap = new HashMap<>();
        bkServiceNotReadyPoolMap = new HashMap<>();
        nodePortsPool = new Stack<>();
        nodeIpMap = new HashMap<>();
        IMOMap = new HashMap<>();

        for(int i=1000; i>0; i--){
            bkServiceIndexPoolStack.push(i);
        }
        for(int i=32000; i>30004; i-=2){
            nodePortsPool.push(i);
        }

        for(String node: NODE_LIST){
            for(String type: IMO_TYPE){
                List<BackupService> bkServiceNotReadyList = new ArrayList<>();
                bkServiceNotReadyPoolMap.put(node+"+"+type, bkServiceNotReadyList);
                List<BackupService> bkServiceReadyList = new ArrayList<>();
                bkServiceReadyPoolMap.put(node+"+"+type, bkServiceReadyList);
            }
        }

        for(int i=0; i<BACKUP_INITIAL_LIMIT; i++) {
            for(String node: NODE_LIST){
                for(String type: IMO_TYPE) {
                    bkServiceRequestQueue.offer(new BackupServiceRequest(node, type));
                }
            }
        }

        nodeIpMap.put(CORE_NODE, "172.17.8.101");
        nodeIpMap.put(EDGE_NODE_1, "172.17.8.102");
        nodeIpMap.put(EDGE_NODE_2, "172.17.8.103");
    }
    public static void AddLog(String sender, String log){

    }
}
