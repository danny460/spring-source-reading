package com.danny.example.spring.sourcereading.core.basicconfig;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Setup {
    public static void main(String[] args){
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("basicConfig.xml");
        SimpleBean bean = context.getBean(SimpleBean.class);
        bean.doSomething();
        context.close();
    }
}
