package com.dg.com.controllertest;

import java.util.HashMap;
import java.util.Map;

public class DgService {
    public static final String[] deploymentList = {"eureka", "zuul", "test", "speed", "oil"};
    public Integer index;
    public String name;
    public String clusterIP;
    public String node;        // the node the dg is running
    public String nodeIP;
    public Integer eureka_node_port;
    public Integer zuul_node_port;
    public Map<String, DgDeployment> deploymentMap;
//    public DgDeployment eureka;
//    public DgDeployment zuul;
//    public DgDeployment test;
//    public DgDeployment speed;
//    public DgDeployment oil;
    public DgService(String name, String nodeIP, Integer eureka_node_port, Integer zuul_node_port){
        this.name = name;
        this.nodeIP = nodeIP;
        this.eureka_node_port = eureka_node_port;
        this.zuul_node_port = zuul_node_port;
        this.deploymentMap = new HashMap<>();
        for(int i=0; i<deploymentList.length; i++){
            deploymentMap.put(deploymentList[i], new DgDeployment(name + "-" + deploymentList[i], false));
        }
//        eureka = new DgDeployment(name + "-eureka", false);
//        zuul = new DgDeployment(name + "-zuul", false);
//        test = new DgDeployment(name + "-test", false);
//        speed = new DgDeployment(name + "-speed", false);
//        oil = new DgDeployment(name + "-oil", false);
    }

}
