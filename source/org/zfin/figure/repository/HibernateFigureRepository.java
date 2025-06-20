package org.zfin.figure.repository;


import jakarta.persistence.Tuple;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.expression.ExpressionFigureStage;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.ComparatorCreator;
import org.zfin.marker.Clone;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.repository.RepositoryFactory;

import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Calendar.YEAR;
import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateFigureRepository implements FigureRepository {

    @Override
    public Figure getFigure(String zdbID) {
        Session session = currentSession();
        return session.get(Figure.class, zdbID);
    }

    /*  for multi-object hql transformations, check out HibernateExpressionRepository.getExperimentFigureStagesByGeneAndFish2 */
    @Override
    public List<Person> getSubmitters(Publication publication, Clone probe) {
        List<Person> submitters = new ArrayList<>();

        String probeZdbID = null;
        if (probe != null) {
            probeZdbID = probe.getZdbID();
        }

        Session session = currentSession();

        String sql = """
              SELECT ids_source_zdb_id
              FROM int_data_source, expression_experiment2
              WHERE xpatex_zdb_id = ids_data_zdb_id
                and xpatex_source_zdb_id = :pubZdbID
                and ids_source_zdb_id like 'ZDB-PERS-%'
            """;

        if (probeZdbID != null) {
            sql += "  and xpatex_probe_feature_zdb_id = :probeZdbID ";
        }

        Query query = session.createNativeQuery(sql);
        query.setParameter("pubZdbID", publication.getZdbID());
        if (probeZdbID != null) {
            query.setParameter("probeZdbID", probeZdbID);
        }


        for (Object o : query.list()) {
            String personZdbID = (String) o;
            Person person = RepositoryFactory.getProfileRepository().getPerson(personZdbID);
            if (person != null && !submitters.contains(person))
                submitters.add(person);
        }

        //these are going to be small lists, sorting here keeps the query & join small
        submitters.sort(ComparatorCreator.orderBy("lastName", "firstName"));

        return submitters;
    }


    @Override
    public List<Figure> getFiguresForDirectSubmissionPublication(Publication publication, Clone probe) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<Figure> query = criteriaBuilder.createQuery(Figure.class);
        Root<Figure> figureRoot = query.from(Figure.class);

        Join<Figure, ExpressionFigureStage> xpatres = figureRoot.join("expressionFigureStage");
        Join<ExpressionFigureStage, ExpressionExperiment2> xpatex = xpatres.join("expressionExperiment");
        Join<ExpressionExperiment2, Publication> pub = xpatex.join("publication");
        Join<ExpressionExperiment2, Clone> cl = xpatex.join("probe");

        Predicate pred1 = criteriaBuilder.equal(pub.get("zdbID"), publication.getZdbID());
        Predicate pred2 = criteriaBuilder.equal(cl.get("zdbID"), probe.getZdbID());
        Predicate andPredicate = criteriaBuilder.and(pred1, pred2);

        query.where(andPredicate);
        query.orderBy(criteriaBuilder.asc(figureRoot.get("label")));
        query.distinct(true);

        List<Figure> figures = currentSession().createQuery(query).getResultList();
        return figures;
    }


    @Override
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

    @Override
    public List<Figure> getAllFigures() {
        String hql = "from Figure";
        return currentSession().createQuery(hql, Figure.class).getResultList();
    }

    @Override
    public List<Image> getAllImagesWithFigures() {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<Image> query = criteriaBuilder.createQuery(Image.class);
        Root<Image> imageRoot = query.from(Image.class);

        query.orderBy(criteriaBuilder.desc(imageRoot.get("zdbID")));
        List<Image> images = currentSession().createQuery(query).getResultList();
        return images;
    }

    @Override
    public List<Image> getRecentlyCuratedImages() {
        String hql = """
            select distinct image, publication.publicationDate
            from Image as image
            inner join image.figure as figure
            inner join figure.publication as publication
            inner join publication.statusHistory as pubStatus
            left outer join figure.expressionFigureStage as figureStage
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
                size(figureStage.expressionResultSet) > 0
                and (
                  figureStage.expressionExperiment.assay.name = 'Immunohistochemistry'
                  or figureStage.expressionExperiment.assay.name = 'mRNA in situ hybridization'
                )
              )
            )
            order by publication.publicationDate desc
            """;

        Calendar oneYearAgo = Calendar.getInstance();
        oneYearAgo.add(YEAR, -1);
        Query<Tuple> query = currentSession().createQuery(hql, Tuple.class);
        query.setParameter("oneYearAgo", oneYearAgo);
        query.setParameter("closedCurated", PublicationTrackingStatus.Name.CLOSED_CURATED);

        return query.list().stream().map(tuple -> tuple.get(0, Image.class))
                .collect(Collectors.toList());
    }

}

