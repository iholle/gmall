package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
    /**
     * 下单
     * @param orderInfo
     * @return
     */
    public String saveOrder(OrderInfo orderInfo);

    /**
     * 生成Token
     * @param userId
     * @return
     */
    public String getToken(String userId);

    /**
     * 验证token
     * @param userId
     * @param token
     * @return
     */
    public boolean verifyToken(String userId,String token);
}
