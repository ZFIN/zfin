package org.zfin.infrastructure.service;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;


public class RequestService {
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    public static String getRequestedUrlPath() {
        HttpServletRequest request = getCurrentRequest();
        return request.getRequestURI();
    }

    public static String getRequestedForwardedUrl() {
        HttpServletRequest request = getCurrentRequest();
        return request.getAttribute("javax.servlet.forward.request_uri").toString();
    }
}