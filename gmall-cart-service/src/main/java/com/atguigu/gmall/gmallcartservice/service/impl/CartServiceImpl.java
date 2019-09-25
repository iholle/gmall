package com.atguigu.gmall.gmallcartservice.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.gmallcartservice.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Reference
    ManageService manageService;

    @Autowired
    RedisUtil redisUtil;


    @Override
    public CartInfo addCart(String userId, String skuId, Integer num) {
        //保存数据库
        //尝试取出已有数据  如果有 更新数量    update  如果没有 新增 insert
        CartInfo cartInfoQuery = new CartInfo();
        cartInfoQuery.setUserId(userId);
        cartInfoQuery.setSkuId(skuId);
        CartInfo cartInfoExists = null;
        cartInfoExists = cartInfoMapper.selectOne(cartInfoQuery);
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);//取出sku信息
        if(cartInfoExists!=null){
            cartInfoExists.setSkuName(skuInfo.getSkuName());
            cartInfoExists.setCartPrice(skuInfo.getPrice());
            cartInfoExists.setSkuNum(cartInfoExists.getSkuNum()+num);
            cartInfoExists.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExists);
        }else{
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExists=cartInfo;
        }
        //加缓存
        //type hash key cart:101:info field skuId value cartInfoJson
        //如果购物车中已有该sku 增加个数 如果没有新增一条
     /*   Jedis jedis = redisUtil.getJedis();
        String cartkey = "cart:"+ userId +":info";
        String cartInfoJson = JSON.toJSONString(cartInfoExists);
        jedis.hset(cartkey,skuId,cartInfoJson);
        jedis.close();*/
        loadCartCache(userId);
        return cartInfoExists;

    }

    @Override
    public List<CartInfo> cartList(String userId) {
        //先查缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = "cart:"+userId+":info";
        List<String> cartJsonList = jedis.hvals(cartKey);
        ArrayList<CartInfo> cartList = new ArrayList<>();
        if(cartJsonList!=null && cartJsonList.size()>0){
            //命中缓存
            for (String cartJson : cartJsonList) {
                CartInfo cartInfo =  JSON.parseObject(cartJson,CartInfo.class);
                cartList.add(cartInfo);
            }
            cartList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o2.getId().compareTo(o1.getId());
                }
            });
            jedis.close();
            return cartList;
        }else{
            //缓存未命中 缓存中没有数据 查询数据库同时同步到缓存中
            return loadCartCache(userId);
        }

    }

    /**
     * 合并购物车
     * @param userIdDest
     * @param userIdOrig
     * @return
     */
    @Override
    public List<CartInfo> mergeCartList(String userIdDest, String userIdOrig) {
        //1、先做合并
        cartInfoMapper.mergeCartList(userIdDest,userIdOrig);
        //2、合并后把临时购物车删除
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userIdOrig);
        cartInfoMapper.delete(cartInfo);
        //3、重新读取数据加载到缓存中
        List<CartInfo> cartInfoList = loadCartCache(userIdDest);
        return cartInfoList;
    }
    //检查缓存是否存在
    public void loadCartCacheIfNotExists(String userId){
        String cartKey = "cart:"+ userId +":info";
        Jedis jedis = redisUtil.getJedis();
        Long ttl = jedis.ttl(cartKey);
        int ttlInt = ttl.intValue();
        jedis.expire(cartKey, ttlInt + 10);
        Boolean exists = jedis.exists(cartKey);
        jedis.close();
        if(!exists){
            loadCartCache(userId);
        }
    }

    @Override
    public void checkCart(String userId, String skuId, String isChecked) {
        loadCartCacheIfNotExists(userId);
        //检查一下缓存是否存在 避免因为缓存失效 造成缓存数据库不一致
        //保存标志
        String cartKey = "cart:"+ userId +":info";
        Jedis jedis = redisUtil.getJedis();
        String cartInfoJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartInfoJsonNew = JSON.toJSONString(cartInfo);
        jedis.hset(cartKey,skuId,cartInfoJsonNew);
        //为了订单结账 把所有勾选中的商品单独存放 存放在一个checked购物车中
        String cartCheckedKey = "cart:"+ userId +":checked";
        if(isChecked.equals("1")){//勾选加入到待结算的购物车中 取消从待结算的购物车中删除
            jedis.hset(cartCheckedKey,skuId,cartInfoJsonNew);
            jedis.expire(cartCheckedKey,60*60);

        }else{
            jedis.hdel(cartCheckedKey,skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCheckedCartList(String userId) {
        String cartCheckedKey = "cart:"+userId+":checked";
        Jedis jedis = redisUtil.getJedis();
        List<String> checkedCartList = jedis.hvals(cartCheckedKey);
        ArrayList<CartInfo> cartInfoList = new ArrayList<>();
        for (String cartInfoJson : checkedCartList) {
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfoList.add(cartInfo);
        }
        jedis.close();
        return cartInfoList;
    }

    public List<CartInfo> loadCartCache(String userId){
        //读取数据库
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithSkuPrice(userId);
        //加载到缓存中
        //为了方便加入redis 把List --》map
        if(cartInfoList!=null && cartInfoList.size()>0){
            Map<String, String> cartMap = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                cartMap.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            Jedis jedis = redisUtil.getJedis();
            String cartkey = "cart:"+ userId +":info";
            jedis.del(cartkey);
            jedis.hmset(cartkey,cartMap);
            jedis.expire(cartkey,60*60*24);
            jedis.close();
        }
        return cartInfoList;
    }
}
