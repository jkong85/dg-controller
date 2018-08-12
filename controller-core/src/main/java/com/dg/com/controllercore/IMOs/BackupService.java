package com.dg.com.controllercore.IMOs;

import com.dg.com.controllercore.ControllerCoreApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkong on 7/25/18.
 */


public class BackupService {
    public String name;
    public String type;
    public String selector;
    public String node;
    public List<Deployment> deploymentsList = new ArrayList<>();
    public String imoName;  // runtime info
    public String dgName;
    public String mongoIP;
    public BackupService(String name, String type, String selector, String node){
        this.name = name;
        this.type = type;
        this.selector = selector;
        this.node = node;
        this.imoName = null;
        this.dgName = null;
        this.mongoIP = "localhost";
        deploymentsList = new ArrayList<>();
    }
    public String toString(){
        String result = " BkService (name: " + name + ", type: " + type + ", selector: " + selector + ", node: " + node + ", binded to (IMO: " + imoName + ", DG: " + dgName + ") )";
        if(deploymentsList.size() == 0){
            result += ", NO deployment!!";
        }
        for(Deployment deploy : deploymentsList){
            result += ", deploy name: "  + deploy.serviceType;
        }
        result += ", MongoDB deployment IP : " + mongoIP;
        return result;
    }
}
