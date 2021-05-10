package org.zfin.wiki.presentation;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.zfin.framework.api.Pagination;

import javax.servlet.http.HttpServletRequest;

public class PaginationArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(Version.class) != null;
    }

    @Override
    public Pagination resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request
                = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        Pagination pagination = new Pagination();
        return pagination;
    }
}