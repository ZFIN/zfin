package org.zfin.profile.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A poor man's BeanUtils for our specific bean copying reasons.
 */
@Service
public class BeanCompareService {

    private Logger logger = Logger.getLogger(BeanCompareService.class);

    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject) throws Exception {
        return compareBeanField(field, oldObject, newObject, String.class, false);
    }

    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject,Class fieldClass) throws Exception {
        return compareBeanField(field, oldObject, newObject, fieldClass, false);
    }

    /**
     * Copies from field old to field new if different and returns a non-empty string if different.
     *
     * @param field
     * @param oldObject
     * @param newObject
     * @return
     */
    public BeanFieldUpdate compareBeanField(String field, Object oldObject, Object newObject, Class fieldClass, boolean doCopy)
            throws Exception {
        Object oldField = PropertyUtils.getProperty(oldObject,field);
        Object newField = PropertyUtils.getProperty(newObject,field);

        BeanFieldUpdate beanFieldUpdate = null;

        if (fieldClass.equals(Boolean.class)
                || fieldClass.equals(boolean.class)
                ) {
            oldField = (oldField == null ? false : oldField);
            newField = (newField == null ? false : newField);
        }

        // TODO: this can only be simpler
        // check for the null comparisons
        if ((oldField == null && newField == null)) {
            return beanFieldUpdate; // null
        } else
        if(!ObjectUtils.equals(oldField,newField)){
            beanFieldUpdate = new BeanFieldUpdate();
            beanFieldUpdate.setField(field);
            beanFieldUpdate.setFrom(oldField);
            beanFieldUpdate.setTo(newField);
            beanFieldUpdate.setFieldType(fieldClass);
            if (doCopy) {
                PropertyUtils.setProperty(oldObject,field,newField);
            }
        } else if (oldField.equals(newField)) {
            // do nothing!
        } else {
            logger.error("not sure how we got here: " + oldField + " " + newField + " " + oldField + " " + newObject + " " + fieldClass);
        }

        return beanFieldUpdate;
    }


    protected String generateGetterNameFromFieldName(String field, Class fieldClass) {
//        return (fieldClass.equals(Boolean.class) || fieldClass.equals(boolean.class) ? "is": "get")
//                + field.substring(0, 1).toUpperCase() + field.substring(1);
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    protected String generateSetterNameFromFieldName(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    protected void applyUpdates(Object objectToBeUpdated, List<BeanFieldUpdate> fields) throws Exception {
        for (BeanFieldUpdate field : fields) {
            applyUpdate(objectToBeUpdated, field);
        }
    }

    protected void applyUpdate(Object objectToBeUpdated, BeanFieldUpdate field) throws Exception{

        PropertyUtils.setProperty(objectToBeUpdated,field.getField(),field.getTo());
    }
}
