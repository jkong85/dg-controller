package com.dg.kj.dgcommons;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

//@SpringBootApplication
public class DgCommonsApplication {

    public static void main(String[] args) {
//        SpringApplication.run(DgCommonsApplication.class, args);
    }
    public static void delay(int n){
        int m = n*1000;
        try { Thread.sleep(m); } catch (InterruptedException ie) {}
    }

    public static String printMap(Map<String, String> map){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry : map.entrySet()){
            sb.append("<");
            sb.append(entry.getKey());
            sb.append(" : ");
            sb.append(entry.getValue());
            sb.append(">\n");
        }
        return sb.toString();
    }
}
