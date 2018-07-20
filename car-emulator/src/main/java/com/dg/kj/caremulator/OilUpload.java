package com.dg.kj.caremulator;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;


public class OilUpload implements Runnable{
    private Thread t;
    private String threadName;
    private String type;
    private static String oilURL = "/oil/cur";

    //private static Integer index = 0;
    private static Integer[] oil;
    private static Integer size;


    OilUpload(String name, String type) {
        threadName = name;
        this.type = type;
        this.size = CarEmulatorApplication.toyota_oil.length;
        oil = new Integer[size];
        for(int i=0; i<size; i++){
            oil[i] = CarEmulatorApplication.honda_oil[i];
            if(type.equals(CarEmulatorApplication.HONDA)){
                oil[i] = CarEmulatorApplication.honda_oil[i];
            }else if(type.equals(CarEmulatorApplication.TOYOTA)){
                oil[i] = CarEmulatorApplication.toyota_oil[i];
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
//        System.out.println("Running Oil upload of " +  threadName );
        try {
            RestTemplate template = new RestTemplate();
            while(DataSync.index < CarEmulatorApplication.toyota_oil.length) {
                int index = DataSync.index;
                // simple check, not rigorouse
                if (CarEmulatorApplication.destination.size() == 0) {
                    System.out.println("No DGs are available, waiting...");
                }

                if(CarEmulatorApplication.destination.size() <= isSent[index].size()){
                    Thread.sleep(100);
                    continue;
                }
                if(!isPrinted[index]) {
                    System.out.println("Upload " + index + " oil data : " + Integer.toString(oil[index]));
                    isPrinted[index] = true;
                }
               for (int i = 0; i < CarEmulatorApplication.destination.size(); i++) {
                    String dstURL = "http://" + CarEmulatorApplication.destination.get(i);
                    if(isSent[index].contains(dstURL)){// current data have been successsfully sent to DST
                        continue;
                    }
                    MultiValueMap<String, Object> oilParamMap = new LinkedMultiValueMap<String, Object>();
                    oilParamMap.add("name", name);
                    oilParamMap.add("type", type);
                    oilParamMap.add("value", Integer.toString(oil[index]));

                    try {
                        template.postForObject(dstURL + oilURL, oilParamMap, String.class);
                        isSent[index].add(dstURL);
                    } catch (RestClientException re) {
//                        System.out.println("Resend oil data!");
                    }
                }
                Thread.sleep(100);
            }
        }catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
//        System.out.println("Oil upload Thread of " +  threadName + " is done.");
    }

    public void start () {
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
