package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加购物车
     * @param userId
     * @param skuId
     * @param num
     * @return
     */
    public CartInfo addCart(String userId, String skuId, Integer num);

    /**
     * 显示购物车列表
     * @param userId
     * @return
     */
    public List<CartInfo> cartList(String userId);

    /**
     * 合并购物车
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    public List<CartInfo> mergeCartList(String userIdDest,String userIdOrig);

    /**
     * 购物车中的商品选准，未选准
     * @param userId
     * @param skuId
     * @param isChecked
     */
    public void checkCart(String userId,String skuId,String isChecked);

    /**
     * 查询用户需要结账的商品清单
     * @param userId
     * @return
     */
    public List<CartInfo> getCheckedCartList(String userId);

}
