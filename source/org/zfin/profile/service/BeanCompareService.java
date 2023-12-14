package org.zfin.profile.service;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.gwt.root.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

/**
 * A poor man's BeanUtils for our specific bean copying reasons.
 */
@Service
public class BeanCompareService {

    private Logger logger = LogManager.getLogger(BeanCompareService.class);

    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject) throws Exception {
        return compareBeanField(field, oldObject, newObject, false);
    }

    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject, boolean doCopy)
        throws Exception {
        return compareBeanField(field, oldObject, newObject, doCopy, false);
    }

    /**
     * Copies from field old to field new if different and returns a non-empty string if different.
     *
     * @param field
     * @param oldObject
     * @param newObject
     * @return
     */
    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject, boolean doCopy, boolean nullAsBoolean)
        throws Exception {
        Object oldField = getProperty(oldObject, field);
        Object newField = getProperty(newObject, field);

        BeanFieldUpdate beanFieldUpdate = null;

        if (nullAsBoolean) {
            oldField = (oldField == null ? false : oldField);
            newField = (newField == null ? false : newField);
        }

        //if both are null (or empty string), then they are equal
        boolean oldFieldIsNull = oldField == null || (oldField instanceof String && StringUtils.isEmpty((String) oldField));
        boolean newFieldIsNull = newField == null || (newField instanceof String && StringUtils.isEmpty((String) newField));
        if (oldFieldIsNull && newFieldIsNull) {
            return null;
        }

        if (!Objects.equals(oldField, newField)) {
            beanFieldUpdate = new BeanFieldUpdate();
            if (oldField instanceof String oldFieldString) {
                if (StringUtils.isEmpty(oldFieldString)) {
                    beanFieldUpdate.setFrom(null);
                } else {
                    beanFieldUpdate.setFrom(oldField);
                }
            } else {
                beanFieldUpdate.setFrom(oldField);
            }
            if (newField instanceof String newFieldString) {
                if (StringUtils.isEmpty(newFieldString)) {
                    beanFieldUpdate.setTo(null);
                } else {
                    beanFieldUpdate.setTo(newField);
                }
            } else {
                beanFieldUpdate.setTo(newField);
            }
            beanFieldUpdate.setField(field);
            if (doCopy) {
                PropertyUtils.setProperty(oldObject, field, newField);
            }
        }

        return beanFieldUpdate;
    }

    public void applyUpdates(Object objectToBeUpdated, List<BeanFieldUpdate> fields) throws Exception {
        for (BeanFieldUpdate field : fields) {
            applyUpdate(objectToBeUpdated, field);
        }
    }

    protected void applyUpdate(Object objectToBeUpdated, BeanFieldUpdate field) throws Exception {
        PropertyUtils.setProperty(objectToBeUpdated, field.getField(), field.getTo());
    }

    private static Object getProperty(Object bean, String field) {
        try {
            return PropertyUtils.getProperty(bean, field);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            return null;
        }
    }
}
