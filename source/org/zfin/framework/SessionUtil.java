package org.zfin.framework;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;

public class SessionUtil {

    public static void setVariable(String name, Object value) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request != null) {
            request.getSession().setAttribute(name, value);
        }
    }

    public static Object getVariable(String name) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request != null) {
            Object sessionVariable = request.getSession().getAttribute(name);
            return sessionVariable;
        }

        //throw exception for object not found
        throw new NoSuchElementException("Session variable not found: " + name);
    }

    public static void removeVariable(String name) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (request != null) {
            request.getSession().removeAttribute(name);
        }
    }
}
