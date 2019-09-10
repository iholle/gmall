package com.atguigu.gmall.gmallmanageservice.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    public List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

}
