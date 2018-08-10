package com.dg.kj.caremulator;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DestinationUpdate implements Runnable{
    private Thread t;
    private String name;
    private String type;
    private static String infoURL = "http://172.17.8.101:30002/core/information?value=";

    DestinationUpdate(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void run() {
        while(true) {
            //String urlService = infoURL + name;
            String urlService = infoURL + name + "&type=" + type + "&location=10";
            RestTemplate restTemplate = new RestTemplate();
            int cnt = 5;
            while(cnt > 0 ) {
                try {
                    String response = restTemplate.getForObject(urlService, String.class);
                    System.out.println("Response of destination request: " + response);
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
                        }
                    }
                    cnt = 0;
                } catch (RestClientException re) {
                    cnt--;
                }
            }
            try {
                Thread.sleep(5000);
            }catch (InterruptedException e) {
                System.out.println("Destination update of " +  name + " interrupted.");
            }
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, name);
            t.start ();
        }
    }
    private boolean isValidIP(String ip){
//        System.out.println("valid IP: " + ip);
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
