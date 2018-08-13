package com.dg.com.controllercore;

import com.dg.com.controllercore.IMOs.BackupService;
import com.dg.com.controllercore.IMOs.BackupServiceRequest;
import com.dg.com.controllercore.IMOs.IMO;
import com.dg.com.controllercore.Tasks.BkServiceCheckAvailNumberThread;
import com.dg.com.controllercore.Tasks.BkServiceCheckDeployReadyThread;
import com.dg.com.controllercore.Tasks.BkServiceCreateThread;
import com.dg.kj.dgcommons.DgCommonsApplication;
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
    //public static final String[] NODE_LIST = {"core"};
    public static final String[] NODE_LIST = {"core", "edge1", "edge2"};

    public static final String HONDA = "honda";
    public static final String TOYOTA = "toyota";
    //public static final String[] IMO_TYPE = {"honda", "toyota"};
    public static String[] IMO_TYPE = {"honda"};

    public static final Integer PORT_POOL_CAPACITY = 20;

    // Normally, we just use one BACKUP_LIMIT
    // I define those two with 1 difference, just for the Demo performance.
    // If we run one car for each type, there is no need to create a new BackupService, even the car moves from one edge node to another edge node frequently
    // Make those two parameter the same for normal case
    public static final Integer BACKUP_INITIAL_LIMIT = 1;
    public static final Integer BACKUP_LIMIT = BACKUP_INITIAL_LIMIT - 1;

    public static Stack<Integer> bkServiceIndexPoolStack;

    public static Map<String, String> nodeIpMap = new HashMap<>();
    // Stack to store all available node port (3000 ~ 4000)
    public static Stack<Integer> nodePortsPool = new Stack<>();

    // Map<node+car_type, List<BackupService>>>
    public static Map<String, List<BackupService>> bkServiceReadyPoolMap = new HashMap<>();
    public static Map<String, List<BackupService>> bkServiceNotReadyPoolMap = new HashMap<>();

    public static Queue<BackupServiceRequest> bkServiceRequestQueue;

    // <bkServiceName, BackupService> // for quick get BackupService by its name
    public static Map<String, BackupService> bkServiceNameMap = new HashMap<>();

    // Global info of car's DGS
    public static Map<String, IMO> IMOMap = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(ControllerCoreApplication.class);

    public static void main(String[] args) {
        Initialize();

        SpringApplication.run(ControllerCoreApplication.class, args);

        DgCommonsApplication.delay(10);

        BkServiceCreateThread bkServiceCreateThread = new BkServiceCreateThread();
        bkServiceCreateThread.start();

        BkServiceCheckAvailNumberThread bkServiceCheckAvailNumberThread = new BkServiceCheckAvailNumberThread();
        bkServiceCheckAvailNumberThread.start();


        DgCommonsApplication.delay(2);

        BkServiceCheckDeployReadyThread bkServiceCheckDeployReadyThread = new BkServiceCheckDeployReadyThread();
        bkServiceCheckDeployReadyThread.start();

    }
    private static void Initialize(){
        logger.info("Start initialize ...");
        bkServiceRequestQueue = new LinkedList<>();
        bkServiceIndexPoolStack = new Stack<>();
        bkServiceReadyPoolMap = new HashMap<>();
        bkServiceNotReadyPoolMap = new HashMap<>();
        bkServiceNameMap = new HashMap<>();
        nodePortsPool = new Stack<>();
        nodeIpMap = new HashMap<>();
        IMOMap = new HashMap<>();

        for(int i=30; i>0; i--){
            bkServiceIndexPoolStack.push(i);
        }
        // 30001, 30002 : controller
        // 30003, 30004 : BKService test
        for(int i=30010+PORT_POOL_CAPACITY; i>30009; i-=2){
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
