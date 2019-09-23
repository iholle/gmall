package com.atguigu.gmall.gmallcartweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {
    @Reference
    CartService cartService;

    @PostMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(@RequestParam("skuId") String skuId, @RequestParam("num") int num, HttpServletRequest request, HttpServletResponse response){
        String userId = (String) request.getAttribute("userId");
        if(userId==null){
            //如果用户未登录 检查cookie用户是否有token 如果有token 通token作为id 加入购物车 如果没有生成一个新的Token放入cookie;
            userId = CookieUtil.getCookieValue(request, "user_tmp_id", false);
            if(userId==null){
                userId = UUID.randomUUID().toString();
                CookieUtil.setCookie(request,response,"user_tmp_id",userId,60*60*24*7,false);
            }

        }

        CartInfo cartInfo = cartService.addCart(userId, skuId, num);
        request.setAttribute("cartInfo",cartInfo);
        request.setAttribute("num",num);
        return "success";
    }

    @GetMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request){
        String userId =(String) request.getAttribute("userId");//查看用户id
        List<CartInfo> cartList = null;
        if(userId!=null){//有登录
            cartList = cartService.cartList(userId);
        }
       String userTmpId = CookieUtil.getCookieValue(request, "user_tmp_id", false);

        List<CartInfo> cartTempList = null;

        if(userTmpId!=null){//如果登录前未登录时,存在临时购物车考虑合并

            cartTempList = cartService.cartList(userTmpId);//如果有临时id,查看是否合并购物车
            cartList = cartTempList;
        }
        if(userId!=null && cartTempList!=null && cartTempList.size()>0){
            cartList = cartService.mergeCartList(userId,userTmpId);//如果有临时购物车进行合并,获得合并后的购物车列表
        }
        request.setAttribute("cartList",cartList);//如果不需要合并,在取登录后的购物车

        return "cartList";
    }
}
