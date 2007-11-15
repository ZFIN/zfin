package org.zfin.framework;

import org.springframework.aop.MethodBeforeAdvice;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Aug 21, 2006
 * Time: 10:29:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class AutomaticMethodLoggin implements MethodBeforeAdvice {

    public void before(Method method, Object[] args, Object target) throws Throwable {
        Logger log = Logger.getLogger(target.getClass());
        System.out.println("Before The method call");
    }
}
