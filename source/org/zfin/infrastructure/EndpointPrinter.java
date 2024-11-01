package org.zfin.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

@Component
public class EndpointPrinter {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public EndpointPrinter(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @PostConstruct
    public void printEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            Set<RequestMethod> requestMethods = mappingInfo.getMethodsCondition().getMethods();
            String patterns = mappingInfo.getPatternsCondition().toString();
            String controller = handlerMethod.getBeanType().getName();
            String method = handlerMethod.getMethod().getName();

            System.out.println("Endpoint: " + patterns + " | HTTP Method: " + requestMethods + " | Controller: " + controller + " | Method: " + method);
        }
    }
}
