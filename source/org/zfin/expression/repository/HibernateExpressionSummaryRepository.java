package org.zfin.expression.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionStageAnatomyContainer;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Gene;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

import java.util.Iterator;

public class HibernateExpressionSummaryRepository implements ExpressionSummaryRepository {


    //TODO: should be in a marker repository
    public Gene getGene(Gene gene) {
        Session session = HibernateUtil.currentSession();
        return (Gene)session.load(Gene.class,gene.getZdbID());

    }

}
