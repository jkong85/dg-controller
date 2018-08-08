package com.dg.com.controllercore.IMOs;

/**
 * Created by jkong on 7/25/18.
 */
public class BackupServiceRequest {
    public String node;
    public String type;
    public BackupServiceRequest(String node, String type){
        this.node = node;
        this.type = type;
    }

    public String toString(){
        return "| BackupServiceRequest: " + node + type + " | ";
    }
}
