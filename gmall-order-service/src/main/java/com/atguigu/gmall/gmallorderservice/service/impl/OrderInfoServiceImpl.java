package com.atguigu.gmall.gmallorderservice.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.gmallorderservice.mapper.OrderDetailMapper;
import com.atguigu.gmall.gmallorderservice.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

@Service
public class OrderInfoServiceImpl implements OrderService {
    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        orderInfoMapper.insertSelective(orderInfo);
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        return orderInfo.getId();
    }

    @Override
    public String getToken(String userId) {
        //token type String key user:1010:trade_code value token
        String token = UUID.randomUUID().toString();
        String tokenKey = "user:"+userId+":trade_code";
        Jedis jedis = redisUtil.getJedis();
        jedis.setex(tokenKey,10*60,token);
        jedis.close();
        return token;
    }

    @Override
    public boolean verifyToken(String userId, String token) {
        String tokenKey = "user:"+userId+":trade_code";
        Jedis jedis = redisUtil.getJedis();
        String tokenExists = jedis.get(tokenKey);
        jedis.watch(tokenKey);
        Transaction transaction = jedis.multi();
        if(tokenExists!=null && tokenExists.equals(token)){
            transaction.del(tokenKey);
        }
        List<Object> list = transaction.exec();
        if(list!=null && list.size()>0 && (Long)list.get(0)==1L){
            return true;
        }else{
            return false;
        }

    }
}
