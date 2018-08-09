package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.ControllerCoreApplication;

/**
 * Created by jkong on 8/9/18.
 */
public class IMOBehavior {
    public IMOBehavior(){}

    public static String getNodeByLocation(String location){
         if(Integer.valueOf(location) < 60) {
            return ControllerCoreApplication.EDGE_NODE_1;
        }else return ControllerCoreApplication.EDGE_NODE_2;
    }

}
