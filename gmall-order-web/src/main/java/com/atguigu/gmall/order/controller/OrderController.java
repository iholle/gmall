package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.enums.OrderStatus;
import com.atguigu.gmall.enums.ProcessStatus;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    ManageService manageService;


    @GetMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){

        String userId =(String) request.getAttribute("userId");
        //获取用户地址列表
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);
        request.setAttribute("userAddressList",userAddressList);

        //用户需要结账的商品清单
        List<CartInfo> checkedCartList = cartService.getCheckedCartList(userId);
        BigDecimal totalAmount = new BigDecimal("0");
        for (CartInfo cartInfo : checkedCartList) {
            BigDecimal cartInfoAmount = cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum()));
            totalAmount = totalAmount.add(cartInfoAmount);
        }
        String token = orderService.getToken(userId);

        request.setAttribute("tradeNo",token);

        request.setAttribute("checkedCartList",checkedCartList);

        request.setAttribute("totalAmount",totalAmount);
        return  "trade";
    }

    @PostMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean isEnableToken = orderService.verifyToken(userId, tradeNo);
        if(!isEnableToken){
            request.setAttribute("errMsg","页面已失效,请重新结算!");
            return "tradeFail";
        }
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.setCreateTime(new Date());
        orderInfo.setExpireTime(DateUtils.addMinutes(new Date(),15));
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
            orderDetail.setImgUrl(skuInfo.getSkuDefaultImg());
            orderDetail.setSkuName(skuInfo.getSkuName());
            //验价
            if(!orderDetail.getOrderPrice().equals(skuInfo.getPrice())){
                request.setAttribute("errMsg","商品价格已发生变动,请重新下单");
                return "tradeFail";
            }
        }

        //验库存 多线程并行执行
        List<OrderDetail> errList = Collections.synchronizedList(new ArrayList<>());
        Stream<CompletableFuture<String>> completableFutureStream = orderDetailList.stream().map(orderDetail -> CompletableFuture.supplyAsync(() -> checkSkuNum(orderDetail)).whenComplete((hasStock, ex) -> {
            //supplyAsync中 添加异步执行的线程处理任务 //whenComplete 添加线程执行完毕后的操作
            if (hasStock.equals("0")) {
                errList.add(orderDetail);
            }
        }));
        //流失处理 相当于把list<integer>里的转化为一个 Future数组,Future可以理解为一个不知道什么时候执行完的异步结果
        CompletableFuture[] completableFutures = completableFutureStream.toArray(CompletableFuture[]::new);
        //归集操作allOf 代表此处线程阻塞 直到线程全部执行完毕 anyOf代表阻塞到只要有一个执行完就可
        CompletableFuture.allOf(completableFutures).join();

        if(errList.size()>0){
            StringBuffer errStringBuffer = new StringBuffer();
            for (OrderDetail orderDetail : errList) {
                errStringBuffer.append("商品:"+orderDetail.getSkuName()+"库存暂时不足!");
            }
            request.setAttribute("errMsg",errStringBuffer.toString());
            return "tradeFail";
        }


        String orderId = orderService.saveOrder(orderInfo);

        //删除购物车信息
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }
    public String checkSkuNum(OrderDetail orderDetail){
        String hasStock = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum());
        return hasStock;
    }

}
