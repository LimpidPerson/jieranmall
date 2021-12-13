package com.ygnn.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                //1、RequestContextHolder拿到刚进来的这个请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    // 老请求的Cookie
                    String cookie = attributes.getRequest().getHeader("Cookie");
                    // 赋给新的请求模板
                    template.header("Cookie", cookie);
                    System.out.println("template.header(\"Cookie\", cookie) = " + template.header("Cookie", cookie));
                }
            }
        };
    }

}
