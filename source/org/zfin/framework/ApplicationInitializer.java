package org.zfin.framework;

import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    // some other methods here

    @Override
    protected String[] getServletMappings() {
        return new String[0];
    }

    @Override
    protected jakarta.servlet.Filter[] getServletFilters() {
        DelegatingFilterProxy delegateFilterProxy = new DelegatingFilterProxy();
        delegateFilterProxy.setTargetBeanName("springSecurityFilterChain");
        return new jakarta.servlet.Filter[]{delegateFilterProxy};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[0];
    }
}