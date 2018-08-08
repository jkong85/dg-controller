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
    public BackupService(String name, String type, String selector, String node){
        this.name = name;
        this.type = type;
        this.selector = selector;
        this.node = node;
        this.status = ControllerCoreApplication.BK_SERVICE_STATUS_NOT_READY;
        deploymentsList = new ArrayList<>();
    }
}
