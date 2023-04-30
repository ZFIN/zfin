package org.zfin.framework.dao;


import org.zfin.framework.entity.BaseEntity;

public abstract class BaseEntityDAO<E extends BaseEntity> {

    protected Class<E> myClass;

    protected BaseEntityDAO(Class<E> myClass) {
        this.myClass = myClass;
    }

}
