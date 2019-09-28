package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.enums.ProcessStatus;

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

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    public OrderInfo getOrderInfo(String orderId);

    /**
     * 修改订单状态
     * @param orderId
     */
    public void updateStatus(String orderId, ProcessStatus processStatus,OrderInfo... orderInfos);
}
