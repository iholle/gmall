package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    /**
     * 查询所有
     * @return
     */
    List<UserInfo> getUserInfoListAll();

    /**
     * 添加
     * @param userInfo
     */
    void addUser(UserInfo userInfo);

    /**
     * 根据id修改
     * @param userInfo
     */
    void updateUser(UserInfo userInfo);

    /**
     * 根据名称修改
     * @param name
     * @param userInfo
     */
    void updateUserByName(String name,UserInfo userInfo);

    /**
     * 根据id删除
     * @param userInfo
     */
    void delUser(UserInfo userInfo);

    /**
     * 查询单个用户
     * @param id
     * @return
     */
    UserInfo getUserInfoById(String id);

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 认证、校验
     * @param userId
     * @return
     */
    Boolean verify(String userId);

    /**
     * 获取用户地址列表信息
     * @param userId
     * @return
     */
    public List<UserAddress> getUserAddressList(String userId);

}
