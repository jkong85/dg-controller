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
    public Integer status; //BK_SERVICE_STATUS_NOT_READY = 0;  BK_SERVICE_STATUS_AVAILABLE = 1;  BK_SERVICE_STATUS_USED = 2;
    public List<Deployment> deploymentsList = new ArrayList<>();
    public String imoName;  // runtime info
    public String dgName;
    public BackupService(String name, String type, String selector, String node){
        this.name = name;
        this.type = type;
        this.selector = selector;
        this.node = node;
        this.status = ControllerCoreApplication.BK_SERVICE_STATUS_NOT_READY;
        this.imoName = null;
        this.dgName = null;
        deploymentsList = new ArrayList<>();
    }
    public String toString(){
        String result = "Backup Service with name: " + name + ", type: " + type + ", selector: " + selector + ", node: " + node + " binded to IMO: " + imoName + ", DG: " + dgName;
        if(deploymentsList.size() == 0){
            result += ", NO deployment!!";
        }
        for(Deployment deploy : deploymentsList){
            result += ", deploy name: "  + deploy.serviceType;
        }
        return result;
    }
}
