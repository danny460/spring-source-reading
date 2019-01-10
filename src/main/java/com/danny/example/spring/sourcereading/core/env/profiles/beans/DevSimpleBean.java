package com.danny.example.spring.sourcereading.core.env.profiles.beans;

public class DevSimpleBean implements SimpleBean {
    public void doSomething() {
        System.out.println("This is SimpleBean in dev profile.");
    }
}
