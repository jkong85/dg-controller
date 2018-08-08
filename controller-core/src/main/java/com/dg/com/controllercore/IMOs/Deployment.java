package com.dg.com.controllercore.IMOs;

/**
 * Created by jkong on 7/25/18.
 */
public class Deployment {
    public String name;
    public String node;
    public Deployment(String name, String node){
        this.name = name;
        this.node = node;
    }
    public String toString(){
        return "Deployment with name:" + name + ", node" + node;
    }
}
