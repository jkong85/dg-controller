package com.dg.com.controllercore.IMOs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jkong on 7/25/18.
 */
public class IMO {
    public String name;
    public String type;
    public List<DG> dgList = new ArrayList<>();
    public IMO(String name, String type){
        this.name = name;
        this.type = type;
        dgList = new ArrayList<>();
    }
    //TODO: print more information
    public String toString(){
        return "IMO(name:" + name + ", type:" + type;
    }
}
