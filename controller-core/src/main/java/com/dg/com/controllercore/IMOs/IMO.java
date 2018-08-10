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
    public DG findDGBkService(String bkService){
        if(dgList == null || dgList.size() == 0)
            return null;
        for(DG item : dgList){
            if(item.bkService.name.equals(bkService)){
                return item;
            }
        }
        return null;
    }

    public DG findDGonNode(String node){
        if(node == null || dgList == null || dgList.size()==0){
            logger.warn(" Null info to find DG on node: " + node);
            return null;
        }
        for(DG item : dgList){
//            if(item.node.equals(node)){
            //TODO: Take care here, we shoud use BkService's node to check
            if(item.bkService.node.equals(node)){
                return item;
            }
        }
        logger.warn(" Failed to find DG on node: " + node + " => " + this.toString());
        return null;
    }

    public String getAllDGIpPort(){
        StringBuilder sb = new StringBuilder();
        if(dgList.isEmpty() || dgList.size() == 0){
            return null;
        }
        DG first = dgList.get(0);
        sb.append(first.nodeIP);
        sb.append(":");
        sb.append(first.nodePort);
        for(int i=1; i<dgList.size(); i++){
            DG dg = dgList.get(i);
            sb.append(",");
            sb.append(dg.nodeIP);
            sb.append(":");
            sb.append(dg.nodePort);
        }
        return sb.toString();
    }

    //TODO: print more information
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" IMO inforation:     ");
        sb.append("name:" + name + ", type:" + type + " ");
        for(DG cur : dgList){
            sb.append(" " + cur.toString());
        }
        return sb.toString();
    }
}
