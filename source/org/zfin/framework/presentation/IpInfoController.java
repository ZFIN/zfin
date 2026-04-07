package org.zfin.framework.presentation;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/devtool")
public class IpInfoController {

    @RequestMapping("/ipinfo")
    public Map<String, Object> ipInfo(HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Original client IP as seen by the proxy
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");

        result.put("clientIp", resolveClientIp(request));
        result.put("remoteAddr", request.getRemoteAddr());
        result.put("remoteHost", request.getRemoteHost());
        result.put("remotePort", request.getRemotePort());

        // Proxy-related headers
        Map<String, String> proxyHeaders = new LinkedHashMap<>();
        proxyHeaders.put("X-Forwarded-For", xForwardedFor);
        proxyHeaders.put("X-Real-IP", xRealIp);
        proxyHeaders.put("X-Forwarded-Proto", request.getHeader("X-Forwarded-Proto"));
        proxyHeaders.put("X-Forwarded-Host", request.getHeader("X-Forwarded-Host"));
        proxyHeaders.put("X-Forwarded-Port", request.getHeader("X-Forwarded-Port"));
        result.put("proxyHeaders", proxyHeaders);

        // Request info
        Map<String, String> requestInfo = new LinkedHashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("scheme", request.getScheme());
        requestInfo.put("serverName", request.getServerName());
        requestInfo.put("serverPort", String.valueOf(request.getServerPort()));
        requestInfo.put("requestURI", request.getRequestURI());
        requestInfo.put("requestURL", request.getRequestURL().toString());
        requestInfo.put("protocol", request.getProtocol());
        requestInfo.put("isSecure", String.valueOf(request.isSecure()));
        result.put("requestInfo", requestInfo);

        // All headers
        Map<String, String> allHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            allHeaders.put(name, request.getHeader(name));
        }
        result.put("allHeaders", allHeaders);

        return result;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // First IP in the chain is the original client
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
