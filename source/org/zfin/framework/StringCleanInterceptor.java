package org.zfin.framework;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Hibernate Interceptor to trim strings and escape characters that informix can't handle
 * <p/>
 * http://jagadesh4java.blogspot.com/2009/10/hibernate-interceptors.html
 */
public class StringCleanInterceptor extends EmptyInterceptor {
    public static Logger logger = Logger.getLogger(StringCleanInterceptor.class);

    public boolean onSave(Object entity, Serializable id, Object[] state,
                          String[] propertyNames, Type[] types) {

        if (state != null) {
            for (int i = 0; i < state.length; i++) {
                state[i] = processState(state[i]);
            }
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }


    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (currentState != null) {
            for (int i = 0; i < currentState.length; i++) {
                currentState[i] = processState(currentState[i]);
            }
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }


    private Object processState(Object state) {
        if (state == null) {
            return null;
        }
        if (state instanceof String) {
            return processState((String) state);
        } else {
            Field[] fields = state.getClass().getDeclaredFields();
            if (fields != null) {
                for (Field field : fields) {
                    if (field.getType() == String.class) {
                        runSetter(field, state, processState(runGetter(field, state)));
                    }
                }
            }
        }
        return state;
    }

    private Object processState(String state) {
        if (logger.isDebugEnabled()) {
            logger.debug("before: " + state);
        }
        state = state.trim();
        if (logger.isDebugEnabled()) {
            logger.debug(" after: " + state);
        }
        return state;
    }

    public static Object runGetter(Field field, Object o) {
        // Find the correct method
        for (Method method : o.getClass().getMethods()) {
            if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    try {
                        return method.invoke(o);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.error("Could not determine method: " + method.getName() + " on class " + o.getClass().getName(), e);
                    }

                }
            }
        }
        return null;
    }

    public static Object runSetter(Field field, Object o, Object value) {
        // Find the correct method
        for (Method method : o.getClass().getMethods()) {
            if ((method.getName().startsWith("set")) && (method.getName().length() == (field.getName().length() + 3))) {
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())) {
                    try {
                        return method.invoke(o, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.error("Could not determine method: " + method.getName() + " on class " + o.getClass().getName(), e);
                    }

                }
            }
        }


        return null;
    }
}

