package com.atguigu.gmall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.mapper.UserInfoMapper;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getUserInfoListAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
        userInfoMapper.updateByExampleSelective(userInfo,example);

    }

    @Override
    public void delUser(UserInfo userInfo) {
        userInfoMapper.deleteByPrimaryKey(userInfo.getId());

    }

    @Override
    public UserInfo getUserInfoById(String id) {
        return userInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //1、比对数据库用户名和密码
        String passwd = userInfo.getPasswd();

        //MD5加密
        String passwdMD5 = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(passwdMD5);

        UserInfo userInfoExists = userInfoMapper.selectOne(userInfo);

        //2、加载缓存
        if(userInfoExists!=null){
            Jedis jedis = redisUtil.getJedis();
            //   type String     key  user:1011:info       value    userInfoJson
            String userKey = userKey_prefix+userInfoExists.getId()+userinfoKey_suffix;
            String userInfoJson = JSON.toJSONString(userInfoExists);
            jedis.setex(userKey,userKey_timeOut,userInfoJson);
            jedis.close();
            return userInfoExists;
        }

        return null;
    }

    @Override
    public Boolean verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = userKey_prefix+userId+userinfoKey_suffix;
        Boolean isLogin = jedis.exists(userKey);
        if(isLogin){
            jedis.expire(userKey,userKey_timeOut);
        }

        jedis.close();
        return isLogin;
    }
}
