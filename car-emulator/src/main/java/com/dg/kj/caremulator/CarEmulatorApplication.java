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
    //private static String registerURL = "http://172.17.8.101:30002/test/register";
    private static String registerURL = "http://172.17.8.101:30002/core/registration";
    public static String HONDA = "honda";
    public static String TOYOTA = "toyota";

    /*
    public static Integer[] honda_speed = {60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61};
    public static Integer[] honda_location = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120};
    public static Integer[] honda_oil = {200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320};

    public static Integer[] toyota_speed = {300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 387, 388, 389, 390, 391, 392, 393, 394, 395, 396, 397, 398, 399, 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 419, 420};
    public static Integer[] toyota_location = {120, 119, 118, 117, 116, 115, 114, 113, 112, 111, 110, 109, 108, 107, 106, 105, 104, 103, 102, 101, 100, 99, 98, 97, 96, 95, 94, 93, 92, 91, 90, 89, 88, 87, 86, 85, 84, 83, 82, 81, 80, 79, 78, 77, 76, 75, 74, 73, 72, 71, 70, 69, 68, 67, 66, 65, 64, 63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
    public static Integer[] toyota_oil = {500, 499, 498, 497, 496, 495, 494, 493, 492, 491, 490, 489, 488, 487, 486, 485, 484, 483, 482, 481, 480, 479, 478, 477, 476, 475, 474, 473, 472, 471, 470, 469, 468, 467, 466, 465, 464, 463, 462, 461, 460, 459, 458, 457, 456, 455, 454, 453, 452, 451, 450, 449, 448, 447, 446, 445, 444, 443, 442, 441, 440, 439, 438, 437, 436, 435, 434, 433, 432, 431, 430, 429, 428, 427, 426, 425, 424, 423, 422, 421, 420, 419, 418, 417, 416, 415, 414, 413, 412, 411, 410, 409, 408, 407, 406, 405, 404, 403, 402, 401, 400, 399, 398, 397, 396, 395, 394, 393, 392, 391, 390, 389, 388, 387, 386, 385, 384, 383, 382, 381};
    */

    public static Integer[] honda_speed = null;
    public static Integer[] honda_location = null;
    public static Integer[] honda_oil = null;

    public static Integer[] toyota_speed = null;
    public static Integer[] toyota_location = null;
    public static Integer[] toyota_oil = null;


    public static List<String> destination = new ArrayList<>();

    public static String NAME = null;
    public static String TYPE = null;
    public static Integer DATA_SIZE = 0;


    public static void main(String[] args) {
        //SpringApplication.run(CarEmulatorApplication.class, args);
        if(args == null || args.length != 2 ){
            System.out.println("Input error! \n java -jar CarEmulator car1 honda|toyota");
            return;
        }
        int locationStart = 0;
        int locationEnd = 51;
        int round = 5;
        DATA_SIZE = generateData(locationStart, locationEnd, round);
        System.out.println("Data size :" + DATA_SIZE.toString());

        String name = args[0];
        String type = args[1];
        NAME = name;
        TYPE = type;

        if(!type.equals(HONDA) && !type.equals(TOYOTA)){
            System.out.println("Car type of " + type + " is not supported!");
        }
        // register the care first
        Integer location = type.equals(HONDA) ? honda_location[0] : toyota_location[0];

        try { Thread.sleep(5000); }catch(InterruptedException ie){ }

        register(name, type, location.toString());

        // pull the destination address per 1 sec
        DestinationUpdate dstThread = new DestinationUpdate(name, type);
        dstThread.start();

        String[] url_ready = new String[2];
        url_ready[0] = "/location/ready";
        if(type.equals(HONDA)){
            url_ready[1] = "/speed/ready";
        }else{
            url_ready[1] = "/oil/ready";
        }
        while(!isReady(url_ready)){
            try{ Thread.sleep(2000);}catch(InterruptedException ie){ }
            System.out.println("DGs of " + name +  " are creating ...");
        }
        System.out.println("DGs are ready to receive data");
        System.out.println("DGs IP address: ");
        for(String dst : destination){
            System.out.println("   " + dst);
        }
        // Data index sync thread
        DataSync dataSync = new DataSync(name);
        dataSync.start();

        // location uploda thread
        LocationUpload locationUpload = new LocationUpload(name, type);
        locationUpload.start();

        // Speed uploda thread
        if(type.equals(HONDA)) {
            SpeedUpload speedUpload = new SpeedUpload(name, type);
            speedUpload.start();
        }

        // oil upload thread
        if(type.equals(TOYOTA)) {
            OilUpload oilUpload = new OilUpload(name, type);
            oilUpload.start();
        }
    }
    // e.g., s=0, e=3, round=2
    // 0, 1, 2, 1, 0, 1, 2, 1, 0   (
    private static int generateData(int s, int e, int r){
        int size =  ((e - s) * 2 - 2)*r + 1;
        System.out.println("Data size is " + size);
        honda_speed = new Integer[size];
        honda_location = new Integer[size];
        honda_oil = new Integer[size];
        toyota_speed = new Integer[size];
        toyota_location = new Integer[size];
        toyota_oil = new Integer[size];

        int honda_oil_base = 400;
        int toyota_oil_base = 600;
        for(int i=0; i<size; i++){
            honda_oil[i] = honda_oil_base - i;
            toyota_oil[i] = toyota_oil_base - i;

            honda_speed[i] = 60;
            toyota_speed[i] = 60;
        }

        int index = 0;
        while(r-- > 0){
            for(int i=s; i<e; i++){
                honda_location[index] = i;
                toyota_location[index] = i;
                index++;
            }
            for(int i=e-2; i>s; i--){
                honda_location[index] = i;
                toyota_location[index] = i;
                index++;
            }
        }
        honda_location[size-1] = s;
        toyota_location[size-1] = s;

        System.out.println("=================" );
        System.out.println("honda_location: " );
        for(int i=0; i<size; i++){
            System.out.print(honda_location[i] + ", ");
        }
        System.out.println();
        System.out.println("honda_speed: " );
        for(int i=0; i<size; i++){
            System.out.print(honda_speed[i] + ", ");
        }

        return size;
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
                System.out.println("Registering " + name + " to DG-Arbiter");
                System.out.println("Registering URL: " + registerURL);
                result = template.postForObject(registerURL, registerParamMap, String.class);
            } catch (RestClientException re) {
                System.out.println(re.toString());
                regResend = true;
                try { Thread.sleep(1000); }catch(InterruptedException ie){ }
            }
        }
        return result;
    }

    private static boolean isReady(String[] url_ready){
        System.out.print("Check whether all services' are ready!");
        if(destination == null || destination.size()==0){
            return false;
        }
        for(int i=0; i<destination.size(); i++){
            RestTemplate restTemplate = new RestTemplate();
            for(String url : url_ready) {
                String dstURL = "http://" + CarEmulatorApplication.destination.get(i) + url;
                System.out.print("Check ready URL: " + dstURL + "   ===> ");
                boolean flag = false;
                for(int j=0; j<5; j++) {
                    try {
                        String response = restTemplate.getForObject(dstURL, String.class);
                        System.out.println(" Ready");
                        flag = true;
                        break;
                    } catch (RestClientException re) {
                        System.out.println(" Not ready");
                    }
                }
                if(!flag){
                    return false;
                }
            }
        }
        return true;
    }
}
