package org.zfin.feature.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.*;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.isNotEmpty;
import org.zfin.Species;
import org.zfin.mutant.Feature;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.infrastructure.AllMarkerNamesFastSearch;
import org.zfin.infrastructure.AllNamesFastSearch;
import org.zfin.infrastructure.DataAlias;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyType;

import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.TextOnlyFigure;
import org.zfin.expression.FigureFigure;
import org.zfin.framework.HibernateUtil;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.util.FilterType;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Hibernate implementation of the Antibody Repository.
 */
public class HibernateFeatureRepository implements FeatureRepository {

    

    public Feature getFeatureByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Feature) session.get(Feature.class, zdbID);
    }
  public DataAlias getSpecificDataAlias(Feature feature, String alias) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(DataAlias.class);
        criteria.add(Restrictions.eq("feature", feature));
        criteria.add(Restrictions.eq("alias", alias));
        return (DataAlias) criteria.uniqueResult();
    }

}

