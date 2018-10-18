package com.hyaroma.sap;

import com.alibaba.fastjson.JSON;
import com.hyaroma.sap.spring.SpringContextUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class SapUtilTest extends  BaseTest{

    @Test
    public void createSend() {
        SapUtil aqSapUtil = SpringContextUtils.getBean("aqSapUtil1", SapUtil.class);
        Map<String,Object> data = new HashMap<>();
        String rfcName = PropertyPlaceholder.getProperty("sap.rfcName").toString();
        aqSapUtil.createSend(rfcName,"00", JSON.toJSONString(data));



        //多客户机测试
//        SapUtil aqSapUtil2 = SpringContextUtils.getBean("aqSapUtil12", SapUtil.class);
//        Map<String,Object> data2 = new HashMap<>();
//        String rfcName2 = PropertyPlaceholder.getProperty("sap.rfcName").toString();
//        aqSapUtil2.createSend(rfcName,"00", JSON.toJSONString(data2));
    }
}