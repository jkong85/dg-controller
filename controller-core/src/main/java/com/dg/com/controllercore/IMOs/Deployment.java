package com.dg.com.controllercore.IMOs;

/**
 * Created by jkong on 7/25/18.
 */
public class Deployment {
    public String name;     // e.g. dg-edge2-honda-3-eureka
    public String node;     // e.g. edge2
    public String serviceType;   // e.g., eureka
    public Deployment (){

    }
    public Deployment(String name, String node, String serviceType){
        this.name = name;
        this.node = node;
        this.serviceType = serviceType;
    }
    public String toString(){
        return "Deployment with name:" + name + ", node" + node + ", " + serviceType;
    }
}
