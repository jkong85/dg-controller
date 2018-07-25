package com.dg.com.controllercore.IMOs;

/**
 * Created by jkong on 7/25/18.
 */
public class DG {
    public String name;
    public String type;
    public String node;
    public String nodeIP;
    public String nodePort;
    public BackupService bkService; // map backup service to DG
    public DG(String name, String type, String node, String nodeIP, String nodePort){
        this.name = name;
        this.type = type;
        this.node = node;
        this.nodeIP = nodeIP;
        this.nodePort = nodePort;
    }
    public DG(String name, String type, String node, String nodeIP, String nodePort, BackupService bkService){
        this.name = name;
        this.type = type;
        this.node = node;
        this.nodeIP = nodeIP;
        this.nodePort = nodePort;
        this.bkService = bkService;
    }
}
