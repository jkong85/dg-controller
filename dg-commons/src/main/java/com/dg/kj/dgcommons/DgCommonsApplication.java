package com.dg.kj.dgcommons;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class DgCommonsApplication {

    public static void main(String[] args) {
//        SpringApplication.run(DgCommonsApplication.class, args);
    }
    public static void delay(int n){
        int m = n*1000;
        try { Thread.sleep(m); } catch (InterruptedException ie) {}
    }
}
