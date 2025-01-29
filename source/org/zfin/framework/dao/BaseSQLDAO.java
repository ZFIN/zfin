package org.zfin.framework.dao;

import lombok.extern.log4j.Log4j2;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.entity.BaseEntity;
import org.zfin.gwt.root.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.IdentifiableType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class BaseSQLDAO<E extends BaseEntity> extends BaseEntityDAO<E> {

    public EntityManager entityManager = HibernateUtil.currentSession();

    protected BaseSQLDAO(Class<E> myClass) {
        super(myClass);
    }

    public E persist(E entity) {
        log.debug("SqlDAO: persist: " + entity);
        entityManager.persist(entity);
        return entity;
    }

    public E find(String id) {
        if (id != null) {
            E entity = entityManager.find(myClass, id);
            return entity;
        } else {
            return null;
        }
    }

    public E find(Long id) {
        log.debug("SqlDAO: find: " + id + " " + myClass);
        if (id != null) {
            E entity = entityManager.find(myClass, id);
            log.debug("Entity Found: " + entity);
            return entity;
        } else {
            log.debug("Input Param is null: " + id);
            return null;
        }
    }


    public SearchResponse<E> findAll(Pagination pagination) {
        log.debug("SqlDAO: findAll: " + myClass);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> findQuery = cb.createQuery(myClass);
        Root<E> rootEntry = findQuery.from(myClass);

        Metamodel metaModel = entityManager.getMetamodel();
        IdentifiableType<E> of = (IdentifiableType<E>) metaModel.managedType(myClass);

        CriteriaQuery<E> all = findQuery.select(rootEntry).orderBy(cb.asc(rootEntry.get(of.getId(of.getIdType().getJavaType()).getName())));
        if (pagination != null && StringUtils.isNotEmpty(pagination.getSortBy())) {
            all.orderBy(cb.desc(rootEntry.get(pagination.getSortBy())));
        }
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(myClass)));
        Long totalResults = entityManager.createQuery(countQuery).getSingleResult();

        TypedQuery<E> allQuery = entityManager.createQuery(all);
        if (pagination != null && pagination.getLimit() != null && pagination.getPage() != null) {
            int first = (pagination.getPage() - 1) * pagination.getLimit();
            if (first < 0)
                first = 0;
            allQuery.setFirstResult(first);
            allQuery.setMaxResults(pagination.getLimit());
        }
        SearchResponse<E> results = new SearchResponse<E>();
        results.setResults(allQuery.getResultList());
        results.setTotalResults(totalResults);
        return results;
    }

    public SearchResponse<E> findByField(String field, Object value) {
        log.debug("SqlDAO: findByField: " + field + " " + value);
        HashMap<String, Object> params = new HashMap<>();
        params.put(field, value);
        SearchResponse<E> results = findByParams(null, params);
        log.debug("Result List: " + results);
        if (results.getResults().size() > 0) {
            return results;
        } else {
            return null;
        }
    }

    public SearchResponse<E> findByParams(Pagination pagination, Map<String, Object> params) {
        return findByParams(pagination, params, null);
    }

    public SearchResponse<E> findByParams(Pagination pagination, Map<String, Object> params, String orderByField) {
        if (orderByField != null) {
            log.debug("Search By Params: " + params + " Order by: " + orderByField + " for class: " + myClass);
        } else {
            log.debug("Search By Params: " + params + " for class: " + myClass);
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = builder.createQuery(myClass);
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<E> root = query.from(myClass);
        Root<E> countRoot = countQuery.from(myClass);

        // System.out.println("Root: " + root);
        List<Predicate> restrictions = new ArrayList<>();
        List<Predicate> countRestrictions = new ArrayList<>();

        for (String key : params.keySet()) {
            Path<Object> column = null;
            Path<Object> countColumn = null;
            log.debug("Key: " + key);
            if (key.contains(".")) {
                String[] objects = key.split("\\.");
                for (String s : objects) {
                    log.debug("Looking up: " + s);
                    if (column != null) {
                        log.debug("Looking up via column: " + s);

                        Path<Object> pathColumn = column.get(s);
                        if (pathColumn.getJavaType().equals(List.class)) {
                            column = ((Join) column).join(s);
                        } else {
                            column = pathColumn;
                        }
                        Path<Object> pathCountColumn = countColumn.get(s);
                        if (pathCountColumn.getJavaType().equals(List.class)) {
                            countColumn = ((Join) countColumn).join(s);
                        } else {
                            countColumn = pathCountColumn;
                        }
                    } else {
                        log.debug("Looking up via root: " + s);
                        column = root.get(s);
                        if (column.getJavaType().equals(List.class)) {
                            column = root.join(s);
                        }
                        countColumn = countRoot.get(s);
                        if (countColumn.getJavaType().equals(List.class)) {
                            countColumn = countRoot.join(s);
                        }
                    }

                    log.debug("Column Alias: " + column.getAlias() + " Column Java Type: " + column.getJavaType() + " Column Model: " + column.getModel() + " Column Type Alias: "
                              + column.type().getAlias() + " Column Parent Path Alias: " + column.getParentPath().getAlias());
                    log.debug("Count Column Alias: " + countColumn.getAlias() + " Count Column Java Type: " + countColumn.getJavaType() + " Count Column Model: " + countColumn.getModel()
                              + " Count Column Type Alias: " + countColumn.type().getAlias() + " Count Column Parent Path Alias: " + countColumn.getParentPath().getAlias());
                }
            } else {
                column = root.get(key);
                countColumn = countRoot.get(key);
            }

            Object value = params.get(key);
            if (value != null) {
                log.debug("Object Type: " + value.getClass());
                if (value instanceof Integer) {
                    log.debug("Integer Type: " + value);
                    Integer desiredValue = (Integer) value;
                    restrictions.add(builder.equal(column, desiredValue));
                    countRestrictions.add(builder.equal(countColumn, desiredValue));
                } else if (value instanceof Enum) {
                    log.debug("Enum Type: " + value);
                    restrictions.add(builder.equal(column, value));
                    countRestrictions.add(builder.equal(countColumn, value));
                } else if (value instanceof Long) {
                    log.debug("Long Type: " + value);
                    Long desiredValue = (Long) value;
                    restrictions.add(builder.equal(column, desiredValue));
                    countRestrictions.add(builder.equal(countColumn, desiredValue));
                } else if (value instanceof Boolean) {
                    log.debug("Boolean Type: " + value);
                    Boolean desiredValue = (Boolean) value;
                    restrictions.add(builder.equal(column, desiredValue));
                    countRestrictions.add(builder.equal(countColumn, desiredValue));
                } else {
                    log.debug("String Type: " + value);
                    String desiredValue = (String) value;
                    restrictions.add(builder.equal(column, desiredValue));
                    countRestrictions.add(builder.equal(countColumn, desiredValue));
                }
            } else {
                restrictions.add(builder.isEmpty(root.get(key)));
                countRestrictions.add(builder.isEmpty(countRoot.get(key)));
            }
        }

        if (orderByField != null) {
            query.orderBy(builder.asc(root.get(orderByField)));
        } else {
            Metamodel metaModel = entityManager.getMetamodel();
            IdentifiableType<E> of = (IdentifiableType<E>) metaModel.managedType(myClass);
            query.orderBy(builder.asc(root.get(of.getId(of.getIdType().getJavaType()).getName())));
        }

        query.where(builder.and(restrictions.toArray(new Predicate[0])));

        countQuery.select(builder.count(countRoot));
        countQuery.where(builder.and(countRestrictions.toArray(new Predicate[0])));
        Long totalResults = entityManager.createQuery(countQuery).getSingleResult();

        TypedQuery<E> allQuery = entityManager.createQuery(query);
        if (pagination != null && pagination.getLimit() != null && pagination.getPage() != null) {
            int first = pagination.getPage() * pagination.getLimit();
            if (first < 0)
                first = 0;
            allQuery.setFirstResult(first);
            allQuery.setMaxResults(pagination.getLimit());
        }

        SearchResponse<E> results = new SearchResponse<E>();
        results.setResults(allQuery.getResultList());
        results.setTotalResults(totalResults);
        return results;

    }


}
