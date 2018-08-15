package com.dg.kj.imolocation.feignclient;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jkong on 8/15/18.
 */
@Component
@FeignClient(value = "imo-speed")
public interface ServiceSpeedFeignclient {
    @RequestMapping(value = "/feigntest")
    String feigntest();
}
