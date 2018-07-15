package com.dg.com.controllertest;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ImoDGs {
    private static final int CAPACITY = 100;
    public String name;
    public Stack<Integer> index;
    public DgService coreDG;
    public List<DgService> edgeDGs;
    public ImoDGs(String name){
        this.name = name;
        index = new Stack<>();
        for(int i=CAPACITY; i>0; i--){
            index.push(i);
        }
        edgeDGs = new ArrayList<>();
    }
    public String getAllDgIpPort(){
        // name:value,name:value
        StringBuilder sb = new StringBuilder();
        sb.append(coreDG.nodeIP);
        sb.append(":");
        sb.append(coreDG.zuul_node_port);
        for(int i=0; i<edgeDGs.size(); i++){
            sb.append(",");
            sb.append(coreDG.nodeIP);
            sb.append(":");
            sb.append(coreDG.zuul_node_port);
        }
        return sb.toString();
    }
}
