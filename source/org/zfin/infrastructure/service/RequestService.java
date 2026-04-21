package org.zfin.infrastructure.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class RequestService {

    /**
     * Resolve the real client IP address, accounting for reverse proxy headers.
     */
    public static String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest();
    }

    public static HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getResponse();
    }

    public static String getRequestedUrlPath() {
        HttpServletRequest request = getCurrentRequest();
        return request.getRequestURI();
    }

    public static String getRequestedForwardedUrl() {
        HttpServletRequest request = getCurrentRequest();
        return request.getAttribute("jakarta.servlet.forward.request_uri").toString();
    }
}