package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    @Reference
    UserService service;

    /**
     * 查询用户信息
     * @param userid
     * @return
     */
    @GetMapping("trade")
    public UserInfo trade(@RequestParam("userid") String userid){

        UserInfo userInfo = service.getUserInfoById(userid);

        return  userInfo;
    }

}
