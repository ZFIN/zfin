package org.zfin.figure.repository;


import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.ComparatorCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static java.util.Calendar.YEAR;

@Repository
public class HibernateFigureRepository implements FigureRepository {
    private static Logger logger = LogManager.getLogger(HibernateFigureRepository.class);

    public Figure getFigure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Figure) session.get(Figure.class, zdbID);
    }

    public Figure getDeepFetchedFigure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from Figure figure " +
                " join fetch figure.expressionResults xpatres" +
                " join fetch xpatres.expressionExperiment xpatex " +
                " join fetch xpatex.gene gene " +
                " join fetch xpatex.genotypeExperiment genox" +
                " join fetch genox.genotype geno " +
                " join fetch genox.experiment exp " +
                " where figure.zdbID = :zdbID";

        Query query = session.createQuery(hql);
        query.setParameter("zdbID",zdbID);

        return (Figure) query.list().get(0);
    }


    /*  for multi-object hql transformations, check out HibernateExpressionRepository.getExperimentFigureStagesByGeneAndFish2 */


    public List<Person> getSubmitters(Publication publication, Clone probe) {
        List<Person> submitters = new ArrayList<>();

        String probeZdbID = null;
        if (probe != null) {
            probeZdbID = probe.getZdbID();
        }
        
        Session session = HibernateUtil.currentSession();

        String sql = "SELECT ids_source_zdb_id " +
                "FROM int_data_source, expression_experiment " +
                "WHERE xpatex_zdb_id = ids_data_zdb_id " +
                "  and xpatex_source_zdb_id = :pubZdbID " +
                "  and substring(ids_source_zdb_id from 1 for 8) = 'ZDB-PERS'";

        if (probeZdbID != null) {
            sql += "  and xpatex_probe_feature_zdb_id = :probeZdbID ";
        }

        Query query = session.createSQLQuery(sql);
        query.setParameter("pubZdbID",publication.getZdbID());
        if (probeZdbID != null) {
            query.setParameter("probeZdbID", probeZdbID);
        }


        for (Object o : query.list()) {
            String personZdbID = (String)o;
            Person person = RepositoryFactory.getProfileRepository().getPerson(personZdbID);
            if (person != null && !submitters.contains(person))
                submitters.add(person);
        }

        //these are going to be small lists, sorting here keeps the query & join small
        Collections.sort(submitters, ComparatorCreator.orderBy("lastName", "firstName"));

        return submitters;

             /* This is the query from the apg:

                   SELECT get_person_full_name(zdb_id),zdb_id
             FROM person, int_data_source, expression_experiment
             WHERE xpatex_probe_feature_zdb_id = '$fxpubdis_probe_zdb_id'
             and xpatex_zdb_id=ids_data_zdb_id
             and ids_source_zdb_id = zdb_id
	     and xpatex_source_zdb_id = '$fxpubdis_zdb_id'
             ORDER by 1;

     */
    }


    public List<Figure> getFiguresForDirectSubmissionPublication(Publication publication, Clone probe) {

        List<Figure> figures = new ArrayList<Figure>();

        Session session = HibernateUtil.currentSession();

        Criteria cr = session.createCriteria(Figure.class, "figure");      // from Figure figure

        cr.createAlias("figure.expressionResults", "xpatres");             // join fetch figure.expressionResults xpatres
        cr.createAlias("xpatres.expressionExperiment", "xpatex");          // join txpatres.expressionExperimen xpatex
        cr.createAlias("xpatex.publication", "publication");               // join xpatex.publication publication
        cr.createAlias("xpatex.probe", "probe");                           // join xpatex.probe probe
        cr.add(Restrictions.and(                                           // where publication.zdbID = pubZdbID and probe.zdbID = probeZDBID
                Restrictions.eq("publication.zdbID", publication.getZdbID()),
                Restrictions.eq("probe.zdbID", probe.getZdbID())
        ));
        cr.addOrder(Order.asc("figure.label"));                            // order by figure.label
        cr.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);            // set distinct without using projections.distinct
//
//        ProjectionList projectionList = Projections.projectionList();      // select distinct figure.zdbID, figure.label
//        projectionList.add(Projections.distinct(Projections.property("figure.zdbID")));
//        projectionList.add(Projections.property("figure.label"));
//        cr.setProjection(projectionList);
//
        figures.addAll(cr.list());

        return figures;

    }

    public List<Image> getImages(List<String> zdbIDs) {
        if (CollectionUtils.isEmpty(zdbIDs)) {
            return Collections.emptyList();
        }
        return HibernateUtil.currentSession()
                .createCriteria(Image.class)
                .add(Restrictions.in("zdbID", zdbIDs)).list();
    }

    public List<Image> getRecentlyCuratedImages() {
        String hql = "select distinct image " +
                "from Image as image " +
                "inner join image.figure as figure " +
                "inner join figure.publication as publication " +
                "inner join publication.statusHistory as pubStatus " +
                "left outer join figure.expressionResults as expression " +
                "left outer join figure.phenotypeExperiments as phenotype " +
                "where pubStatus.isCurrent = true " +
                "and pubStatus.status.name = :closedCurated " +
                "and pubStatus.date > :oneYearAgo " +
                "and publication.publicationDate > :oneYearAgo " +
                "and publication.canShowImages = true " +
                "and image.imageFilename is not null " +
                "and ( " +
                "  phenotype.id is not null " +
                "  or ( " +
                "    expression.xpatresID is not null " +
                "    and (" +
                "      expression.expressionExperiment.assay.name = 'Immunohistochemistry' " +
                "      or expression.expressionExperiment.assay.name = 'mRNA in situ hybridization' " +
                "    ) " +
                "  ) " +
                ") ";

        Query query = HibernateUtil.currentSession().createQuery(hql);

        Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(YEAR, -1);
        query.setParameter("oneYearAgo", oneYearAgo);

//        Calendar twoYearsAgo = Calendar.getInstance();
//        twoYearsAgo.add(YEAR, -2);
//        query.setParameter("twoYearsAgo", twoYearsAgo);

        query.setParameter("closedCurated", PublicationTrackingStatus.Name.CLOSED_CURATED);

        // TODO: figure out how to shuffle the list in the query instead of returning the whole list here

        return query.list();
    }

}

