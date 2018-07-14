package com.dg.com.controllertest;

public class DgService {
    Integer index;
    String name;
    String clusterIP;
    String nodeIP;
    String eureka_node_port;
    String zuul_node_port;
    DgDeployment eureka;
    DgDeployment zuul;
    DgDeployment test;
    DgDeployment speed;
    DgDeployment oil;
    public DgService(String name, String eureka_node_port, String zuul_node_port){
        this.name = name;
        this.eureka_node_port = eureka_node_port;
        this.zuul_node_port = zuul_node_port;
        eureka = new DgDeployment(name + "-eureka", false);
        zuul = new DgDeployment(name + "-zuul", false);
        test = new DgDeployment(name + "-test", false);
        speed = new DgDeployment(name + "-speed", false);
        oil = new DgDeployment(name + "-oil", false);
    }

}
