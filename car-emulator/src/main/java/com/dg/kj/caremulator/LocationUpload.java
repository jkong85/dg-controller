package com.dg.kj.caremulator;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.swing.event.CaretEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class LocationUpload implements Runnable{
    private Thread t;
    private String threadName;
    private String type;
    private static String locationURL = "/location/cur";

    //private static Integer index = 0;
    private static Integer[] location;
    private static Integer size;


    LocationUpload(String name, String type) {
        threadName = name;
        this.type = type;
        this.size = CarEmulatorApplication.toyota_location.length;
        location = new Integer[size];
        for(int i=0; i<size; i++){
            location[i] = CarEmulatorApplication.honda_location[i];
            if(type.equals(CarEmulatorApplication.HONDA)){
                location[i] = CarEmulatorApplication.honda_location[i];
            }else if(type.equals(CarEmulatorApplication.TOYOTA)){
                location[i] = CarEmulatorApplication.toyota_location[i];
            }
        }
        System.out.println("Location upload initializating " +  threadName );
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
//        System.out.println("Running Location upload of " +  threadName );
        try {
            RestTemplate template = new RestTemplate();
            while(DataSync.index < CarEmulatorApplication.toyota_location.length) {
                int index = DataSync.index;
                // simple check, not rigorouse
                if (CarEmulatorApplication.destination.size() == 0) {
                    System.out.println("Location Data: no DGs are available, waiting...");
                }

                if(CarEmulatorApplication.destination.size() <= isSent[index].size()){
                    Thread.sleep(100);
                    continue;
                }

                if(!isPrinted[index]) {
                    System.out.println("Upload " + index + " location data : " + Integer.toString(location[index]));
                    isPrinted[index] = true;
                }
               for (int i = 0; i < CarEmulatorApplication.destination.size(); i++) {
                    String dstURL = "http://" + CarEmulatorApplication.destination.get(i);
                    if(isSent[index].contains(dstURL)){// current data have been successsfully sent to DST
                        continue;
                    }
                    MultiValueMap<String, Object> locationParamMap = new LinkedMultiValueMap<String, Object>();
                    locationParamMap.add("name", name);
                    locationParamMap.add("type", type);
                    locationParamMap.add("value", Integer.toString(location[index]));

                    try {
                        template.postForObject(dstURL + locationURL, locationParamMap, String.class);
                        isSent[index].add(dstURL);
                    } catch (RestClientException re) {
//                        System.out.println("Resend location data!");
                    }
                }
                Thread.sleep(100);
            }
        }catch (InterruptedException e) {
            System.out.println("Thread " +  threadName + " interrupted.");
        }
//        System.out.println("Location upload Thread of " +  threadName + " is done.");
    }

    public void start () {
//        System.out.println("Starting location upload of " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
