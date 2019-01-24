package org.zfin.fish.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.springframework.jdbc.object.SqlQuery;
import org.springframework.stereotype.Repository;
import org.zfin.fish.WarehouseSummary;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Fish;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateFishRepository implements FishRepository {

    public Set<ZfinFigureEntity> getAllFigures(String fishZdbID) {
        String sql = "select phenox_fig_zdb_id, " +
                "        CASE" +
                "         WHEN img_fig_zdb_id is not null then 'true' " +
                "         ELSE 'false' " +
                "        END as hasImage " +
                "from phenotype_experiment " +
                "     join fish_experiment on phenox_genox_zdb_id = genox_zdb_id " +
                "     left outer join image on img_fig_zdb_id = phenox_fig_zdb_id " +
                "where genox_fish_zdb_id = :fishZdbID ";
        Session session = HibernateUtil.currentSession();
        Query query = session.createSQLQuery(sql);
        query.setParameter("fishZdbID", fishZdbID);
        List<Object[]> fishObjects = query.getResultList();
        if (fishObjects == null)
            return null;

        Set<ZfinFigureEntity> zfinFigureEntities = new HashSet<>(fishObjects.size());
        for (Object[] annotationObj : fishObjects) {
            ZfinFigureEntity zfinFigureEntity = new ZfinFigureEntity();
            zfinFigureEntity.setID((String) annotationObj[0]);
            zfinFigureEntity.setHasImage(Boolean.parseBoolean(((String) annotationObj[1]).trim()));
            zfinFigureEntities.add(zfinFigureEntity);
        }
        return zfinFigureEntities;
    }


    @Override
    public Fish getFishByName(String name) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(Fish.class);
        criteria.add(Restrictions.eq("name", name));
        return (Fish) criteria.uniqueResult();
    }


    /**
     * Retrieve the Warehouse summary info for a given mart.
     *
     * @param mart mart
     * @return warehouse summary
     */
    @Override
    public WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(WarehouseSummary.class);
        criteria.add(Restrictions.eq("martName", mart.getName()));
        return (WarehouseSummary) criteria.uniqueResult();
    }

}
