package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

public interface PaymentService {
    /**
     * 保存支付信息
     * @param paymentInfo
     */
    public void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 查询支付信息
     * @param paymentInfoQuery
     * @return
     */
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /**
     * 更新支付信息
     * @param outTradeNo
     * @param paymentInfo
     */
    public void updatePaymentInfoByOutTradeNo(String outTradeNo,PaymentInfo paymentInfo);

    /**
     * 消息发送
     * @param orderId
     * @param result
     */
    public void sendPaymentToOrder(String orderId,String result);
}
