package com.dg.kj.caremulator;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


public class InfoUpload implements Runnable{
    private Thread t;
    private String threadName;
    private String type;
    private static String speedURL = "/speed/cur";
    private static String locationURL = "/location/cur";
    private static String oilURL = "/oil/cur";


    private static Integer index = 0;
    private static Integer[] speed;
    private static Integer[] location;
    private static Integer[] oil;
    private static Integer size;


    InfoUpload(String name, String type) {
        threadName = name;
        this.type = type;
        this.size = CarEmulatorApplication.toyota_location.length;
        speed = new Integer[size];
        oil = new Integer[size];
        location = new Integer[size];
        if(type.equals(CarEmulatorApplication.HONDA)){
            for(int i=0; i<size; i++){
                speed[i] = CarEmulatorApplication.honda_speed[i];
                oil[i] = CarEmulatorApplication.honda_oil[i];
                location[i] = CarEmulatorApplication.honda_location[i];
            }
        }else if(type.equals(CarEmulatorApplication.TOYOTA)){
            for(int i=0; i<size; i++) {
                speed[i] = CarEmulatorApplication.toyota_speed[i];
                oil[i] = CarEmulatorApplication.toyota_oil[i];
                location[i] = CarEmulatorApplication.toyota_location[i];
            }
        }
        System.out.println("Creating Infoupload of " +  threadName );
    }

    public void run() {
        String name = CarEmulatorApplication.NAME;
        String type = CarEmulatorApplication.TYPE;
        System.out.println("Running InfoUploda of " +  threadName );
        try {
            RestTemplate template = new RestTemplate();
            while(index < size) {
                System.out.println(index + " data is available");
                if (CarEmulatorApplication.destination.size() == 0) {
                    System.out.println("No DGs are available, waiting...");
                }
                for (int i = 0; i < CarEmulatorApplication.destination.size(); i++) {
                    String dstURL = "http://" + CarEmulatorApplication.destination.get(i);
                    MultiValueMap<String, Object> speedParamMap = new LinkedMultiValueMap<String, Object>();
                    MultiValueMap<String, Object> locationParamMap = new LinkedMultiValueMap<String, Object>();
                    //MultiValueMap<String, Object> oilParamMap = new LinkedMultiValueMap<String, Object>();
                    speedParamMap.add("name", name);
                    speedParamMap.add("type", type);
                    speedParamMap.add("value", speed[i]);
                    //oilParamMap.add("name", name);
                    //oilParamMap.add("type", type);
                    //oilParamMap.add("value", oil[i]);
                    locationParamMap.add("name", name);
                    locationParamMap.add("type", type);
                    locationParamMap.add("value", location[i]);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    boolean speedResend = true;
                    boolean oilResend = true;
                    boolean locationResend = true;
                    while(speedResend || oilResend || locationResend) {
                        if(speedResend) {
                            speedResend = false;
                            try {
                                HttpEntity<String> httpEntity = new HttpEntity<>(speedParamMap.toString(), headers);
                                String response = template.postForObject(dstURL + speedURL, httpEntity, String.class);
                            } catch (RestClientException re) {
                                System.out.println("Resend speed data!");
                                speedResend = true;
                            }
                        }
                        //if(oilResend) {
                        //    oilResend = false;
                        //    try {
                        //        template.postForObject(dstURL + oilURL, oilParamMap, String.class);
                        //    } catch (RestClientException re) {
                        //        System.out.println("Resend oil data!");
                        //        oilResend = true;
                        //    }
                        //}
                        if(locationResend) {
                            locationResend = false;
                            try {
                                HttpEntity<String> httpEntity = new HttpEntity<>(locationParamMap.toString(), headers);
                                template.postForObject(dstURL + locationURL, httpEntity, String.class);
                            } catch (RestClientException re) {
                                System.out.println("Resend location data!");
                                locationResend = true;
                            }
                        }

                        Thread.sleep(5000);
                    }
                }
                index++;
                Thread.sleep(5000);

            }
        }catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
        System.out.println("InfoUpload Thread of " +  threadName + " is done.");
    }

    public void start () {
        System.out.println("Starting InfoUploda of " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
