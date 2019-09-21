package com.atguigu.gmall.gmallpassportweb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.atguigu.gmall.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }
    @Test
    public void testJwt(){
        Map<String, Object> map = new HashMap<>();
        map.put("userId","123");
        map.put("nickName","zhang3");
        String token = JwtUtil.encode("atguigu", map, "192.168.121.128");
        System.out.println(token);
        Map<String, Object> map1 = JwtUtil.decode(token, "atguigu", "192.168.121.128");

        System.out.println(map1);
    }


}
