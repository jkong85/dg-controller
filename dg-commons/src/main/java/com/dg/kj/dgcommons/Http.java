package com.dg.kj.dgcommons;

import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class Http {
    public Http(){

    }
    public static String httpPost(String url, MultiValueMap<String, Object> paraMap, int cnt){
        RestTemplate template = new RestTemplate();
        boolean retry = true;
        String result = null;
        while(retry && cnt-->0){
            try {
                result = template.postForObject(url, paraMap, String.class);
                retry = false;
            }catch(RestClientException re) {
                retry = true;
                System.out.println(re);
            }
            try{
                Thread.sleep(200);
            }catch (InterruptedException ie){
            }
        }
        return result;
    }
}
