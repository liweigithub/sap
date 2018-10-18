package com.hyaroma.sap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wstv
 * spring 加载配置文件
 */
public class PropertyPlaceholder extends PropertyPlaceholderConfigurer {
    private static Map<String,String> propertyMap;


    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        propertyMap = new HashMap<String, String>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            String value = props.getProperty(keyStr);
            propertyMap.put(keyStr, value);
        }
    }
    //static method for accessing context properties
    public static Object getProperty(String name) {
        return propertyMap.get(name);
    }

    public static void setProperty(String key,String value) {
         propertyMap.put(key,value);
    }
}
