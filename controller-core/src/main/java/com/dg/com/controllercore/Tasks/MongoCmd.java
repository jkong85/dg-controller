package com.dg.com.controllercore.Tasks;

import com.dg.com.controllercore.IMOs.DG;
import com.dg.kj.dgcommons.DgCommonsApplication;
import com.dg.kj.dgcommons.Http;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Created by jkong on 8/12/18.
 */
public class MongoCmd {
    private static final Logger logger = LogManager.getLogger(MongoCmd.class);

    public static final String MONGO_OPS_PORT = "8080";
    public static final String MONGO_OPS_API_CLEAN = "/cleanmongo";     // API refer to http.go
    public static final String MONGO_OPS_API_CLONE = "/clonemongo";      // API refer to http.go

    public static final String MONGO_DB_PORT = "27017";     // It is used in application.properties in our micro-service module

    //
    public static boolean migrateMongoDB(DG srcDg, DG dstDG){
        if(srcDg == null || dstDG == null || srcDg == dstDG){
            logger.warn(" Source DG or Destination DG is null or SrcDG == DstDG , Do nothing!");
            return true;
        }
        String srcDGMongoIp = srcDg.bkService.mongoIP;
        String dstDGMongoIP = dstDG.bkService.mongoIP;
        //String mongoPort = "8080";
        logger.debug("Copy from src MongDB (IP:" + srcDGMongoIp + " to dst MongDB (IP:" + dstDGMongoIP + ")");

        MultiValueMap<String, Object> mongoParamMap = new LinkedMultiValueMap<String, Object>();
        mongoParamMap.add("ip", srcDGMongoIp);
        String mongoURL = getMongoOpsURL(dstDGMongoIP, MONGO_OPS_API_CLONE);
        logger.debug("URL of dstDG mongo is: " + mongoURL);

        Http.httpPost(mongoURL, mongoParamMap, 1);

        //TODO: mechanism to check whether clone is done??
        //Wait some time for clone completion
        DgCommonsApplication.delay(5);
        logger.debug("Successfully migrate MongoDB from " + srcDg.name + " to " + dstDG.name);
        return true;
    }

    public static void cleanMongo(DG dg){
        String dgMongoIp = dg.bkService.mongoIP;
        String url = getMongoOpsURL(dgMongoIp, MONGO_OPS_API_CLEAN);
        logger.debug("Clean test db of DG: " + dg.name + " with bkService:" + dg.bkService.name + ", mongIP: " + dgMongoIp + " with URL: " + url);
        Http.httpGet(url);
    }
    private static String getMongoOpsURL(String ip, String api){
        return "http://" + ip + ":" + MONGO_OPS_PORT + "/" + api;
    }
}
