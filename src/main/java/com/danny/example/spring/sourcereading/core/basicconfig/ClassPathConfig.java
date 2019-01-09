package com.danny.example.spring.sourcereading.core.basicconfig;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClassPathConfig {
    public static void main(String[] args){
        System.setProperty("testPlaceHolder", "classpath");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("${testPlaceHolder}:classpathConfig.xml");
        SimpleBean bean = context.getBean(SimpleBean.class);
        bean.doSomething();
        context.close();
    }
}
