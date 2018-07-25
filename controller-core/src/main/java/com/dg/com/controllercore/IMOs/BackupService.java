package com.dg.com.controllercore.IMOs;

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
    public String status; // 0: not ready, 1: not used, 2: used
    public List<Deployment> deploymentsList = new ArrayList<>();
    public BackupService(String name, String type, String selector, String node){
        this.name = name;
        this.type = type;
        this.selector = selector;
        this.node = node;
        deploymentsList = new ArrayList<>();
    }
}
