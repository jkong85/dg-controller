package com.dg.kj.caremulator;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;


public class SpeedUpload implements Runnable{
    private Thread t;
    private String threadName;
    private String type;
    private static String speedURL = "/speed/cur";

    //private static Integer index = 0;
    private static Integer[] speed;
    private static Integer size;


    SpeedUpload(String name, String type) {
        threadName = name;
        this.type = type;
        this.size = CarEmulatorApplication.honda_location.length;
        speed = new Integer[size];
        for(int i=0; i<size; i++){
            speed[i] = CarEmulatorApplication.honda_speed[i];
            if(type.equals(CarEmulatorApplication.HONDA)){
                speed[i] = CarEmulatorApplication.honda_speed[i];
            }else if(type.equals(CarEmulatorApplication.TOYOTA)){
                speed[i] = CarEmulatorApplication.toyota_speed[i];
            }
        }
    }

    public void run() {
        String name = CarEmulatorApplication.NAME;
        String type = CarEmulatorApplication.TYPE;

        Set<String>[] isSent = new HashSet[CarEmulatorApplication.honda_location.length +1];
        boolean[] isPrinted = new boolean[CarEmulatorApplication.honda_location.length + 1];
        for(int i=0; i< isSent.length; i++){
            isSent[i] = new HashSet<>();
            isPrinted[i] = false;
        }
//        System.out.println("Running Speed upload of " +  threadName );
        try {
            RestTemplate template = new RestTemplate();
            while(DataSync.index < CarEmulatorApplication.toyota_speed.length) {
                int index = DataSync.index;
                // simple check, not rigorouse
                if (CarEmulatorApplication.destination.size() == 0) {
                    System.out.println("NO DGs are available, waiting...");
                }

                if(CarEmulatorApplication.destination.size() <= isSent[index].size()){
                    Thread.sleep(100);
                    continue;
                }
                if(!isPrinted[index]) {
                    System.out.println("Upload " + index + " speed data : " + Integer.toString(speed[index]));
                    isPrinted[index] = true;
                }
               for (int i = 0; i < CarEmulatorApplication.destination.size(); i++) {
                    String dstURL = "http://" + CarEmulatorApplication.destination.get(i);
                    if(isSent[index].contains(dstURL)){// current data have been successsfully sent to DST
                        continue;
                    }
                    MultiValueMap<String, Object> speedParamMap = new LinkedMultiValueMap<String, Object>();
                    speedParamMap.add("name", name);
                    speedParamMap.add("type", type);
                    speedParamMap.add("value", Integer.toString(speed[index]));

                    try {
                        template.postForObject(dstURL + speedURL, speedParamMap, String.class);
                        isSent[index].add(dstURL);
                    } catch (RestClientException re) {
//                        System.out.println("Resend speed data!");
                    }
                }
                Thread.sleep(100);
            }
        }catch (InterruptedException e) {
//            System.out.println("Thread " +  threadName + " interrupted.");
        }
        System.out.println("Speed upload Thread of " +  threadName + " is done.");
    }

    public void start () {
//        System.out.println("Starting speed upload of " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
