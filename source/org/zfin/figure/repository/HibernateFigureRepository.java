package org.zfin.figure.repository;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.ComparatorCreator;
import org.zfin.marker.Clone;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.repository.RepositoryFactory;

import javax.persistence.criteria.*;
import java.util.*;

import static java.util.Calendar.YEAR;
import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateFigureRepository implements FigureRepository {
    private static Logger logger = LogManager.getLogger(HibernateFigureRepository.class);

    public Figure getFigure(String zdbID) {
        Session session = currentSession();
        return session.get(Figure.class, zdbID);
    }

    /*  for multi-object hql transformations, check out HibernateExpressionRepository.getExperimentFigureStagesByGeneAndFish2 */

    public List<Person> getSubmitters(Publication publication, Clone probe) {
        List<Person> submitters = new ArrayList<>();

        String probeZdbID = null;
        if (probe != null) {
            probeZdbID = probe.getZdbID();
        }
        
        Session session = currentSession();

        String sql = """
                        SELECT ids_source_zdb_id 
                        FROM int_data_source, expression_experiment 
                        WHERE xpatex_zdb_id = ids_data_zdb_id 
                          and xpatex_source_zdb_id = :pubZdbID 
                          and ids_source_zdb_id like 'ZDB-PERS-%'
                      """;

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
    }


    public List<Figure> getFiguresForDirectSubmissionPublication(Publication publication, Clone probe) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<Figure> query = criteriaBuilder.createQuery(Figure.class);
        Root<Figure> figureRoot = query.from(Figure.class);

        Join<Figure, ExpressionResult> xpatres = figureRoot.join("expressionResults");
        Join<ExpressionResult, ExpressionExperiment> xpatex = xpatres.join("expressionExperiment");
        Join<ExpressionExperiment, Publication> pub = xpatex.join("publication");
        Join<ExpressionExperiment, Clone> cl = xpatex.join("probe");

        Predicate pred1 = criteriaBuilder.equal(pub.get("zdbID"), publication.getZdbID());
        Predicate pred2 = criteriaBuilder.equal(cl.get("zdbID"), probe.getZdbID());
        Predicate andPredicate = criteriaBuilder.and(pred1, pred2);

        query.where(andPredicate);
        query.orderBy(criteriaBuilder.asc(figureRoot.get("label")));
        query.distinct(true);

        List<Figure> figures = currentSession().createQuery(query).getResultList();
        return figures;
    }


    public List<Image> getImages(List<String> zdbIDs) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<Image> query = criteriaBuilder.createQuery(Image.class);
        Root<Image> imageRoot = query.from(Image.class);

        query.where(imageRoot.get("zdbID").in(zdbIDs));

        List<Image> images = currentSession().createQuery(query).getResultList();

        return images.stream()
                .sorted(Comparator.comparing(img -> zdbIDs.indexOf(img.getZdbID())))
                .toList();
    }

    public List<Image> getRecentlyCuratedImages() {
        String hql = """
                select distinct image 
                from Image as image 
                inner join image.figure as figure 
                inner join figure.publication as publication 
                inner join publication.statusHistory as pubStatus 
                left outer join figure.expressionResults as expression 
                left outer join figure.phenotypeExperiments as phenotype 
                where pubStatus.isCurrent = true 
                and pubStatus.status.name = :closedCurated 
                and pubStatus.date > :oneYearAgo 
                and publication.publicationDate > :oneYearAgo 
                and publication.canShowImages = true 
                and image.imageFilename is not null 
                and ( 
                  phenotype.id is not null 
                  or ( 
                    expression.xpatresID is not null 
                    and (
                      expression.expressionExperiment.assay.name = 'Immunohistochemistry' 
                      or expression.expressionExperiment.assay.name = 'mRNA in situ hybridization' 
                    ) 
                  ) 
                ) 
                """;

        Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(YEAR, -1);
        Query query = currentSession().createQuery(hql, Image.class);
        query.setParameter("oneYearAgo", oneYearAgo);
        query.setParameter("closedCurated", PublicationTrackingStatus.Name.CLOSED_CURATED);

        return query.list();
    }

}

