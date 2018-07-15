package com.dg.com.controllertest;

public class DgDeployment {
    public boolean isDeployed;
    public String name;
    public String podName;
    public String podIP;
    public DgDeployment(String name, boolean isDeployed){
        this.name = name;
        this.isDeployed = isDeployed;
        this.podName = null;
        this.podIP = null;
    }

}
