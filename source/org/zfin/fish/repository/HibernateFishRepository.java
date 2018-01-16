package org.zfin.fish.repository;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
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

    private static Logger logger = Logger.getLogger(HibernateFishRepository.class);

    public Set<ZfinFigureEntity> getAllFigures(String fishZdbID) {
        String sql = "select phenox_fig_zdb_id,\n" +
                "        CASE\n" +
                "         WHEN img_fig_zdb_id is not null then 'true'\n" +
                "         ELSE 'false'\n" +
                "        END as hasImage\n" +
                "from phenotype_experiment\n" +
                "     join fish_experiment on phenox_genox_zdb_id = genox_zdb_id\n" +
                "     left outer join image on img_fig_zdb_id = phenox_fig_zdb_id\n" +
                "where genox_fish_zdb_id = :fishZdbID " ;
               /* "UNION\n" +
                "select xedg_fig_zdb_id,\n" +
                "        CASE\n" +
                "         WHEN img_fig_zdb_id is not null then 'true'\n" +
                "         ELSE 'false'\n" +
                "        END as hasImage\n" +
                "from xpat_exp_details_generated\n" +
                "     join fish_experiment on xedg_genox_zdb_id = genox_zdb_id\n" +
                "     left outer join image on img_fig_zdb_id = xedg_fig_zdb_id\n" +
                "where genox_fish_zdb_id = :fishZdbID ;"*/;
        Session session = HibernateUtil.currentSession();
        Query query = session.createSQLQuery(sql);
        query.setParameter("fishZdbID", fishZdbID);
        List<Object[]> fishObjects = query.list();
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
        criteria.add(Restrictions.eq("name",name));
        return (Fish)criteria.uniqueResult();
    }



    /**
     * retrieve all figures for given fish id
     *
     * @param fishID fish ID
     * @return set of figures
     */
    public Set<ZfinFigureEntity> getPhenotypeFigures(String fishID) {
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select pfiggm_member_name, pfiggm_member_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true' " +
                " else 'false'" +
                "END as hasImage " +
                "from phenotype_figure_group_member, phenotype_figure_group, figure, OUTER image " +
                "where pfiggm_group_id = pfigg_group_pk_id " +
                "and pfigg_genox_zdb_id = :fishID " +
                "and fig_zdb_id = pfiggm_member_id " +
                "and img_fig_zdb_id = fig_zdb_id";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("fishID", fishID);
        List<Object[]> objs = sqlQuery.list();
        if (objs == null)
            return null;

        Set<ZfinFigureEntity> figures = new HashSet<>(objs.size());
        if (objs.size() > 0) {
            for (Object[] groupMember : objs) {
                ZfinFigureEntity figure = new ZfinFigureEntity();
                figure.setName((String) groupMember[0]);
                figure.setID((String) groupMember[1]);
                figure.setHasImage(Boolean.parseBoolean((String) groupMember[2]));
                figures.add(figure);
            }
        }
        return figures;
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
