package com.dg.kj.caremulator;

import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DestinationUpdate implements Runnable{
    private Thread t;
    private String name;
    private String type;
    private static String infoURL = "http://172.17.8.101:30002/test/info?value=";

    DestinationUpdate(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void run() {
        System.out.println("Running dst update of " +  name );
        try {
            String urlService = infoURL + name;
            RestTemplate restTemplate = new RestTemplate();
            while(true) {
                String response = restTemplate.getForObject(urlService, String.class);
                System.out.println("response from INFO cmd: " + response);
                // shoud return: "IP1:port1, IP2:port2,..."
                String[] ipPort = response.split(",");
                boolean isValid = ipPort == null ? false : true;
                for (String str : ipPort) {
                    if (!isValidIP(str.split(":")[0])) {
                        isValid = false;
                    }
                }
                if (isValid) {
                    CarEmulatorApplication.destination.clear();
                    System.out.println("Destination of the DGs are :");
                    for (String str : ipPort) {
                        CarEmulatorApplication.destination.add(str);
                        System.out.println(str + "\n");
                    }
                }
                Thread.sleep(5000);
            }
        }catch (InterruptedException e) {
            System.out.println("Destination update of " +  name + " interrupted.");
        }
        System.out.println("DstUpdate Thread of " +  name + " is done.");
    }

    public void start () {
        System.out.println("Starting destinatioUpdat of " +  name );
        if (t == null) {
            t = new Thread (this, name);
            t.start ();
        }
    }
    private boolean isValidIP(String ip){
        System.out.println("valid IP: " + ip);
        if(ip==null || ip.length()<7){
            return false;
        }
        // REGX is a better way
        String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
//            System.out.println("IP is: " + matcher.group());
            return true;
        }
        return false;
    }
}
