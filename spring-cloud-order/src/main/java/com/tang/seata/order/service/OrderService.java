package com.tang.lcn.order.service;

import com.tang.lcn.order.consumer.StockFeign;
import com.tang.lcn.order.domain.Order;
import com.tang.lcn.order.mapper.OrderMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Classname OrderService
 * @Description [ TODO ]
 * @Author Tang
 * @Date 2019/12/24 17:00
 * @Created by ASUS
 */
@Service
public class OrderService {

    @Resource
    private StockFeign stockFeign;

    @Resource
    private OrderMapper orderMapper;

    @GlobalTransactional
    @Transactional(rollbackFor = {RuntimeException.class})
    public void add(Order order, Integer i) {

        orderMapper.insert(order);

        String value = stockFeign.reduceNumber();

        System.out.println( value );

        int a = 1 / i;

    }

}