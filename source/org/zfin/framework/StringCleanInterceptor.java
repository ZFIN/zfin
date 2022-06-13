package org.zfin.framework;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Hibernate Interceptor to trim strings and escape characters that informix can't handle
 * <p/>
 * http://jagadesh4java.blogspot.com/2009/10/hibernate-interceptors.html
 */
public class StringCleanInterceptor extends EmptyInterceptor {
    public static Logger logger = LogManager.getLogger(StringCleanInterceptor.class);

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
                    // modifier = 9 means static field
                    if (field.getType() == String.class && !Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
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
        if (o.getClass().isEnum())
            return null;
        try {
            return BeanUtils.getProperty(o, field.getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.info(e);
        }
        return null;
    }

    public static void runSetter(Field field, Object o, Object value) {
        if (o.getClass().isEnum())
            return;
        // exclude 'public static final' fields....
        if (field.getModifiers() == 25)
            return;
        try {
            // do not update existing nulls
            if (BeanUtils.getProperty(o, field.getName()) == null && value == null)
                return;
            BeanUtils.setProperty(o, field.getName(), value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.info(e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}

