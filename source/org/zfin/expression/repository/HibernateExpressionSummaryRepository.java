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


    public ExpressionStageAnatomyContainer getExpressionStages(Gene gene) {
        Session session = HibernateUtil.currentSession();

        //List<ExpressionStageAnatomy> myXSAresults = new ArrayList<ExpressionStageAnatomy>();

        //query in expressions.hbm.xml
        Query query = session.getNamedQuery("stageanatomyfigure");
        query.setParameter("geneZdbID",gene.getZdbID());
        query.setParameter("unknown", Term.UNSPECIFIED);
        query.setParameter("unspecified",DevelopmentStage.UNKNOWN);

        Iterator stagesAndAnatomy = query.list().iterator();
        ExpressionStageAnatomyContainer xsac = new ExpressionStageAnatomyContainer();

        //the container object will handle duplication produced in the query.
        while(stagesAndAnatomy.hasNext()) {
            Object[] tuple = (Object[]) stagesAndAnatomy.next();

            DevelopmentStage stage = (DevelopmentStage) tuple[0];
            GenericTerm anat = (GenericTerm) tuple[1];
            Figure fig = (Figure) tuple[2];

            xsac.add(stage,anat,fig);

        }


        return xsac;

    }

    //TODO: should be in a marker repository
    public Gene getGene(Gene gene) {
        Session session = HibernateUtil.currentSession();
        return (Gene)session.load(Gene.class,gene.getZdbID());

    }

}
