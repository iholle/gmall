package com.atguigu.gmall.gmallmanageweb.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class MangeController {
    @Reference
    ManageService manageService;

    @PostMapping("getCatalog1")
    public List<BaseCatalog1> getBaseCatalog1(){
        List<BaseCatalog1> catalog1 = manageService.getCatalog1();
        return  catalog1;
    }
    @PostMapping("getCatalog2")
    public List<BaseCatalog2> getBaseCatalog2(String catalog1Id){
        List<BaseCatalog2> catalog2 = manageService.getCatalog2(catalog1Id);
        return catalog2;
    }

    @PostMapping("getCatalog3")
    public List<BaseCatalog3> getBaseCatalog3(String catalog2Id){
        List<BaseCatalog3> catalog3 = manageService.getCatalog3(catalog2Id);
        return catalog3;
    }

    @GetMapping("attrInfoList")
    public List<BaseAttrInfo> getBaseAttrInfoList(String catalog3Id){
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;
    }

    @PostMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    @PostMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        return attrValueList;
    }
}
