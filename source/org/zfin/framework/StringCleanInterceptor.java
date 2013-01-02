package org.zfin.framework;

import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.zfin.util.ZfinStringUtils;

import java.io.Serializable;

/**
 * Hibernate Interceptor to trim strings and escape characters that informix can't handle
 *
 * http://jagadesh4java.blogspot.com/2009/10/hibernate-interceptors.html
 */
public class StringCleanInterceptor extends EmptyInterceptor {
    public static Logger logger = Logger.getLogger(StringCleanInterceptor.class);

    public boolean onSave(Object entity, Serializable id, Object[] state,
                          String[] propertyNames, Type[] types) {

        if (state != null) {
            for(int i=0 ; i < state.length ; i++) {
                state[i] = processState(state[i]);
            }
        }
        return super.onSave(entity, id, state, propertyNames, types);
    }


    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (currentState != null) {
            for(int i=0 ; i < currentState.length ; i++) {
                currentState[i] = processState(currentState[i]);
            }
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
    }



    private Object processState(Object state) {

        if(state != null && state instanceof String) {
            String obtainedValue=(String)state;
            logger.debug("before: " + obtainedValue);
            obtainedValue = ZfinStringUtils.escapeHighUnicode(obtainedValue);
            state=obtainedValue.trim();
            logger.debug(" after: " + state);
        }
        return state;
    }

}

