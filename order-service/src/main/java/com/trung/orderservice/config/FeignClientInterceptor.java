package com.trung.orderservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null){
            HttpServletRequest request = attributes.getRequest();
            List<String> headersToForward = Arrays.asList(
                    "Authorization",
                    "X-UserId",
                    "X-Email",
                    "X-Username",
                    "X-Roles",
                    "X-Permissions"
            );

            for (String headerName : headersToForward) {
                String headerValue = request.getHeader(headerName);
                if (headerValue != null) {
                    template.header(headerName, headerValue);
                }
            }
        }
    }
}
