package com.tang.lcn.stock.service;

import com.tang.lcn.stock.domain.Stock;
import com.tang.lcn.stock.mapper.StockMapper;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Classname StockService
 * @Description [ TODO ]
 * @Author Tang
 * @Date 2019/12/24 18:24
 * @Created by ASUS
 */
@Service
public class StockService {

    @Resource
    private StockMapper stockMapper;

    @GlobalTransactional(name = "create-order",rollbackFor = RuntimeException.class)
    @Transactional(rollbackFor = RuntimeException.class)
    public void update() {

        Stock stock = stockMapper.selectByPrimaryKey(1);
        // 更新记录
        stock.setStock( stock.getStock() - 1 );

        stockMapper.updateByPrimaryKey(stock);

    }

}