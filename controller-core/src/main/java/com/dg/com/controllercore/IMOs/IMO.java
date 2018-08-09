package com.dg.com.controllercore.IMOs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkong on 7/25/18.
 */
public class IMO {
    private static final Logger logger = LogManager.getLogger(IMO.class);
    public String name;
    public String type;
    public List<DG> dgList = new ArrayList<>();
    public IMO(String name, String type){
        this.name = name;
        this.type = type;
        dgList = new ArrayList<>();
    }

    public DG findDGonNode(String node){
        if(node == null || dgList == null || dgList.size()==0){
            return null;
        }
        for(DG item : dgList){
            if(item.node.equals(node)){
                return item;
            }
        }
        return null;
    }

    public String getAllDGIpPort(){
        StringBuilder sb = new StringBuilder();
        for(DG dg : dgList){
            sb.append(dg.nodeIP);
            sb.append(":");
            sb.append(dg.nodePort);
        }
        return sb.toString();
    }

    //TODO: print more information
    public String toString() {
        return "IMO(name:" + name + ", type:" + type + "... should add more)";
    }
}
