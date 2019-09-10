package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

public interface ManageService {
    //查询一级分类
    public List<BaseCatalog1> getCatalog1();

    //根据一级分类id 查询二级分类
    public List<BaseCatalog2>getCatalog2(String catalog1Id);

    //根据二级分类id 查询三级分类
    public List<BaseCatalog3>getCatalog3(String catalog2Id);

    //根据三级分类id 查询平台属性

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    //保存平台属性

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    //根据平台属性id 查询平台属性的详情 顺便把该属性的属性值列表也取到
    public  BaseAttrInfo getBaseAttrInfo(String attrId);

    //根据三级分类id查询 查询商品信息
    public List<SpuInfo> getSpuInfoList(String catalog3Id);

    //获得基本销售属性
    public List<BaseSaleAttr> getBaseSaleAttrList();

    //保存spu信息
    public void saveSpuInfo(SpuInfo spuInfo);

    //根据spuId查询图片列表
    public List<SpuImage> getSpuImageList(String spuId);

    //根据spuId查询销售属性
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //保存skuInfo
    public void saveSkuInfo(SkuInfo skuInfo);
}
