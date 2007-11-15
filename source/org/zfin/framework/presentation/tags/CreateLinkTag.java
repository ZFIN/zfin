package org.zfin.framework.presentation.tags;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GenotypePresentation;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.mutant.Genotype;
import org.zfin.orthology.OrthologySpecies;
import org.zfin.orthology.presentation.OrthologyPresentation;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.sequence.Accession;
import org.zfin.sequence.presentation.AccessionPresentation;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.presentation.RunPresentation;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/** This tag class needs to be expanded to support a "type" attribute which allows it to be used for any
 bean
 */

/**
 * Tag that creates a hyper link for an object of type provided.
 */
public class CreateLinkTag extends TagSupport {

    private Object entity;

    public int doStartTag() throws JspException {

        Object o = getEntity();

        String link;
        if (o instanceof Marker)
            link = MarkerPresentation.getLink((Marker) o);
        else if (o instanceof RunCandidate)
            link = RunCandidatePresentation.getLink((RunCandidate) o);
        else if (o instanceof Run)
            link = RunPresentation.getLink((Run) o);
        else if (o instanceof Accession)
            link = AccessionPresentation.getLink((Accession) o);
        else if (o instanceof AnatomyItem)
            link = AnatomyItemPresentation.getLink((AnatomyItem) o);
        else if (o instanceof Publication)
            link = PublicationPresentation.getLink((Publication) o);
        else if (o instanceof OrthologySpecies)
            link = OrthologyPresentation.getLink((OrthologySpecies) o);
        else if (o instanceof Genotype)
            link = GenotypePresentation.getLink((Genotype) o);
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


}
