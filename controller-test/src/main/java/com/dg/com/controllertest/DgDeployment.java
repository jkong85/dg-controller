package com.dg.com.controllertest;

public class DgDeployment {
    boolean isDeployed;
    String name;
    String podName;
    String podIP;
    public DgDeployment(String name, boolean isDeployed){
        this.name = name;
        this.isDeployed = isDeployed;
        this.podName = null;
        this.podIP = null;
    }

}
