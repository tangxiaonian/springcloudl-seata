package com.tang.lcn.order.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Classname StockFeign
 * @Description [ TODO ]
 * @Author Tang
 * @Date 2019/12/23 19:31
 * @Created by ASUS
 */
@FeignClient(value="SPRING-CLOUD-STOCK-8083")
public interface StockFeign {

    @GetMapping("/stock/reduce")
    public String reduceNumber();
}
