package com.dg.kj.dgcommons;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import java.util.LinkedList;
import java.util.Queue;

public class Log {
    private String type;
    private String sender;
    private String url;
    private int cnt = 3;

    private static final String URL_CONTROLLER_LOG = "http://172.17.8.101:30002/test/logwrite";

    public Log(String type, String sender, int cnt) {
        this.type = type;
        this.sender = sender;
        this.url = URL_CONTROLLER_LOG;
        this.cnt = cnt;
    }

    public void logUpload(String log){
        MultiValueMap<String, Object> logParamMap = new LinkedMultiValueMap<String, Object>();
        logParamMap.add("sender", sender);
        logParamMap.add("log", log);

        Http.httpPost(url, logParamMap, cnt);
    }
}
