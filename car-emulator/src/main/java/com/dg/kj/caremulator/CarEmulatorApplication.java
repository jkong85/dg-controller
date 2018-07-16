package com.dg.kj.caremulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class CarEmulatorApplication {
    private static String registerURL = "http://172.17.8.101:30002/test/register";
    public static String HONDA = "honda";
    public static String TOYOTA = "toyota";

    public static Integer[] honda_speed = {10, 14, 20, 30, 25, 40, 20, 60, 70};
    public static Integer[] honda_location = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static Integer[] honda_oil = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    public static Integer[] toyota_speed = {10, 14, 20, 30, 25, 40, 20, 60, 70};
    public static Integer[] toyota_location = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    public static Integer[] toyota_oil = {1, 2, 3, 4, 5, 6, 7, 8, 9};


    public static List<String> destination = new ArrayList<>();
    public static Integer index = 0;

    public static String NAME = null;
    public static String TYPE = null;


    public static void main(String[] args) {
        //SpringApplication.run(CarEmulatorApplication.class, args);

        if(args == null || args.length != 3){
            System.out.println("Input error! \n java -jar CarEmulator car1 honda|toyota y");
            return;
        }

        String name = args[0];
        String type = args[1];
        NAME = name;
        TYPE = type;

        if(!type.equals(HONDA) && !type.equals(TOYOTA)){
            System.out.println("Car type of " + type + " is not supported!");
        }
        // register the care first
        Integer location = type==HONDA? honda_location[0] : toyota_location[0];
        if(args[2].equals("y")) {
            register(name, type, location.toString() );
        }

        // pull the destination address per 1 sec
        DestinationUpdate dstThread = new DestinationUpdate(name, type);
        dstThread.start();
        // upload the info to DGs
        InfoUpload infoThread = new InfoUpload(name, type);
        infoThread.start();

    }
    private static String register(String name, String type, String location) {
        MultiValueMap<String, Object> registerParamMap = new LinkedMultiValueMap<String, Object>();
        registerParamMap.add("name", name);
        registerParamMap.add("type", type);
        registerParamMap.add("location", location);

        RestTemplate template = new RestTemplate();
        String result = null;

        boolean regResend = true;
        while(regResend) {
            regResend = false;
            try {
                result = template.postForObject(registerURL, registerParamMap, String.class);
            } catch (RestClientException re) {
                System.out.println("Resend register cmd : " + re.toString());
                regResend = true;
                try {
                    Thread.sleep(5000);
                }catch(InterruptedException ie){

                }
            }
        }
        return result;


    }
}
