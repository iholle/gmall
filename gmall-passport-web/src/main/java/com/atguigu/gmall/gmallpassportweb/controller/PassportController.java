package com.atguigu.gmall.gmallpassportweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.atguigu.gmall.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    UserService userService;

    String jwtKey="atguigu";//定义秘钥

    @GetMapping("index")
    public String index(@RequestParam("originUrl") String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "index";
    }

    @PostMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){
        UserInfo userInfoExists = userService.login(userInfo);
        if(userInfoExists!=null){
            //生成Token
            Map<String, Object> map = new HashMap<>();
            map.put("userId",userInfoExists.getId());
            map.put("nickName",userInfoExists.getNickName());
            System.out.println(request.getRemoteAddr());//打印获取到的用户真实ip地址
            //如果有反向代理; 在反想代理中进行配置 把用户的真实ip地址传递过来
            //获取用户真实ip地址
            String ipAddr = request.getHeader("X-forwarded-for");

            String token = JwtUtil.encode(jwtKey, map, ipAddr);

            return token;

        }
        return "fail";
    }


    @GetMapping("verify")
    @ResponseBody
    public String verify(@RequestParam("token") String token,@RequestParam("currentIP") String currentIP){
        //1、验证token
        Map<String, Object> userMap = JwtUtil.decode(token, jwtKey, currentIP);

        //2、验证缓存
        if(userMap!=null){
            String userId =(String) userMap.get("userId");
            Boolean isLogin = userService.verify(userId);
            if(isLogin){
                return "success";
            }
        }
        return "fail";
    }

}
