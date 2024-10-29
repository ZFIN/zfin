package org.zfin.infrastructure.service;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


public class RequestService {
    public static HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static String getRequestedUrlPath() {
        HttpServletRequest request = getCurrentRequest();
        return request.getRequestURI();
    }
}