package com.dg.kj.dgcommons;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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

    public static String httpGet(String urlService){
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        RestTemplate restTemplate = new RestTemplate();
        String str = restTemplate.getForObject(urlService, String.class);
        return str;

        //ResponseEntity<String> response = restTemplate.getForEntity(urlService, String.class);
        //return response.getBody();
    }

    public static boolean httpDelete(String urlService) throws HttpClientErrorException {
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.delete(urlService);
        System.out.println("Successful delete : " + urlService);
        return true;
    }
    public static String httpPost(String urlService, String body) throws HttpClientErrorException{
        String accessToken = "/var/run/secrets/kubernetes.io/serviceaccount/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization","Bearer "+accessToken);

        HttpEntity<String> httpEntity = new HttpEntity<String>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        String str = restTemplate.postForObject(urlService, httpEntity, String.class);
        return str;
    }

}
