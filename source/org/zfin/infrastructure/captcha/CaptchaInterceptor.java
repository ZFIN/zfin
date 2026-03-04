package org.zfin.infrastructure.captcha;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

public class CaptchaInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (!handlerMethod.hasMethodAnnotation(RequiresCaptcha.class)) {
            return true;
        }
        Optional<String> redirectUrl = CaptchaService.getRedirectUrlIfNeeded(request);
        if (redirectUrl.isPresent()) {
            response.sendRedirect(redirectUrl.get());
            return false;
        }
        return true;
    }
}
