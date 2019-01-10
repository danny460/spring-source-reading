package com.danny.example.spring.sourcereading.core.env.profiles;

import com.danny.example.spring.sourcereading.core.env.profiles.beans.SimpleBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProfileConfig {
    public static void main(String[] args){
        ClassPathXmlApplicationContext context;
        SimpleBean bean;

        // startup with dev profile
        System.setProperty("spring.profiles.active", "dev");
        context = new ClassPathXmlApplicationContext("classpath:profilesConfig.xml");
        bean = context.getBean(SimpleBean.class);
        bean.doSomething();
        context.close();

        // startup with production profile, switch to dev.
        System.setProperty("spring.profiles.active", "production");
        context = new ClassPathXmlApplicationContext("classpath:profilesConfig.xml");
        bean = context.getBean(SimpleBean.class);
        bean.doSomething();

        context.getEnvironment().setActiveProfiles("dev"); // not recommended to use this.
        context.refresh();
        bean = context.getBean(SimpleBean.class);
        bean.doSomething();
        context.close();
    }
}
