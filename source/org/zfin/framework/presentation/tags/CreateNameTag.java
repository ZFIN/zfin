package org.zfin.framework.presentation.tags;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.anatomy.presentation.DevelopmentStagePresentation;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.presentation.ExperimentConditionPresentation;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.expression.presentation.ExpressionStatementPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.mutant.presentation.GenotypePresentation;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.ontology.presentation.ExpressionResultPresentation;
import org.zfin.ontology.presentation.PhenotypePresentation;
import org.zfin.ontology.presentation.TermPresentation;
import org.zfin.sequence.reno.RunCandidate;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Tag that creates a hyper link for an object of type provided.
 */
public class CreateNameTag extends TagSupport {

    private Object entity;
    private boolean longVersion;


    public int doStartTag() throws JspException {

        Object o = getEntity();
        String link;
        if (o instanceof Marker)
            link = MarkerPresentation.getName((Marker) o);
        else if (o instanceof Genotype)
            link = GenotypePresentation.getName((Genotype) o);            
        else if (o instanceof PhenotypeStatement)
            link = PhenotypePresentation.getName((PhenotypeStatement) o);
        else if (o instanceof RunCandidate)
            link = RunCandidatePresentation.getName((RunCandidate) o);
        else if (o instanceof AnatomyItem)
            link = AnatomyItemPresentation.getName((AnatomyItem) o);
        else if (o instanceof ExpressionResult)
            link = ExpressionResultPresentation.getName((ExpressionResult) o);
        else if (o instanceof ExpressionStatement)
            link = ExpressionStatementPresentation.getName((ExpressionStatement) o);
        else if (o instanceof Experiment)
            link = ExperimentPresentation.getName((Experiment) o);
        else if (o instanceof ExperimentCondition)
            link = ExperimentConditionPresentation.getName((ExperimentCondition) o);
        else if (o instanceof DevelopmentStage)
            link = DevelopmentStagePresentation.getName((DevelopmentStage) o, longVersion);
        else if (o instanceof Term)
            link = TermPresentation.getName((Term) o);
        else if (o instanceof PostComposedEntity)
            link = TermPresentation.getName((PostComposedEntity) o);
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());

        try {
            pageContext.getOut().print(link);
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return Tag.EVAL_PAGE;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public boolean isLongVersion() {
        return longVersion;
    }

    public void setLongVersion(boolean longVersion) {
        this.longVersion = longVersion;
    }
    
}
