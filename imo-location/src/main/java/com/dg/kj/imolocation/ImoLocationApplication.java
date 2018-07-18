package com.dg.kj.imolocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@SpringBootApplication
@EnableDiscoveryClient
public class ImoLocationApplication {

    public static List<Integer> locationHistoryData = new ArrayList<>();
    public static Queue<Integer> logQueue = new LinkedList<>(); // Just for log
    public static String curServiceName;
    public static String curNode;

    public static final String URL_CONTROLLER_LOG = "http://172.17.8.101:30002/test/logwrite";

    public static void main(String[] args) {
        locationHistoryData = new ArrayList<>();
        logQueue = new LinkedList<>();

        curServiceName = System.getenv("SERVICE_LABEL");
        curNode = System.getenv("CUR_NODE");

        SpringApplication.run(ImoLocationApplication.class, args);

        LogThread logThread = new LogThread("Upload log to controller");
        logThread.start();

        MigrationCopy migrationCopyThread = new MigrationCopy(" Monitoring the location ");
        migrationCopyThread.start();
    }

    public static void httpPost(String url, MultiValueMap<String, Object> paraMap, int cnt){
        RestTemplate template = new RestTemplate();
        boolean retry = true;
        while(retry && cnt-->0){
            try {
                String result = template.postForObject(url, paraMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{
                Thread.sleep(200);
            }catch (InterruptedException ie){
            }
        }
    }
    public static void logUpload(String sender, String log, int cnt){
        MultiValueMap<String, Object> logParamMap = new LinkedMultiValueMap<String, Object>();
        logParamMap.add("sender", sender);
        logParamMap.add("log", log);
        System.out.println("Upload location log : " + sender + " : " + log);

        httpPost(URL_CONTROLLER_LOG, logParamMap, cnt);
    }
}
