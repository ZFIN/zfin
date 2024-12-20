package org.zfin.framework.services;


import org.zfin.framework.api.ObjectResponse;
import org.zfin.framework.dao.BaseEntityDAO;
import org.zfin.framework.dao.BaseSQLDAO;
import org.zfin.framework.entity.BaseEntity;

public class BaseEntityCrudService<E extends BaseEntity, D extends BaseEntityDAO<E>> {

    protected BaseSQLDAO<E> dao;

    protected void setSQLDao(BaseSQLDAO<E> dao) {
        this.dao = dao;
    }

    public ObjectResponse<E> create(E entity) {
        E object = dao.persist(entity);
        ObjectResponse<E> ret = new ObjectResponse<E>(object);
        return ret;
    }

}
