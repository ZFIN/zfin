package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyItemPresentation;
import org.zfin.anatomy.presentation.DevelopmentStagePresentation;
import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.presentation.ExperimentConditionPresentation;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GenotypePresentation;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.presentation.GoTermPresentation;
import org.zfin.orthology.OrthologySpecies;
import org.zfin.orthology.presentation.OrthologyPresentation;
import org.zfin.people.Organization;
import org.zfin.people.presentation.SourcePresentation;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.publication.presentation.ImagePresentation;
import org.zfin.publication.presentation.PublicationPresentation;
import org.zfin.sequence.Accession;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.blast.presentation.BlastLinkPresentation;
import org.zfin.sequence.blast.results.view.HitViewBean;
import org.zfin.sequence.presentation.AccessionPresentation;
import org.zfin.sequence.presentation.DBLinkPresentation;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.presentation.RunPresentation;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;
import java.util.Collection;

/** This tag class needs to be expanded to support a "type" attribute which allows it to be used for any
 bean
 */

/**
 * Tag that creates a hyper link for an object of type provided.
 */
public class CreateLinkTag extends BodyTagSupport {

    private static Logger LOG = Logger.getLogger(CreateLinkTag.class);
    private Object entity;
    private String name;
    private boolean longVersion;

    private Logger logger = Logger.getLogger(CreateLinkTag.class);

    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    private String createLinkFromSingleDomainObject(Object o) throws JspException {

        String link;
        if (o instanceof Marker)  //handling of marker subtypes is taken care of in the getLink method
            link = MarkerPresentation.getLink((Marker) o);
        else if (o instanceof RelatedMarker)
            link = MarkerPresentation.getLink(((RelatedMarker) o).getMarker());
        else if (o instanceof RunCandidate)
            link = RunCandidatePresentation.getLink((RunCandidate) o);
        else if (o instanceof Run)
            link = RunPresentation.getLink((Run) o);
        else if (o instanceof HitViewBean)
            link = BlastLinkPresentation.getLink((HitViewBean) o);
        else if (o instanceof DBLink)
            link = DBLinkPresentation.getLink((DBLink) o);
        else if (o instanceof Accession)
            link = AccessionPresentation.getLink((Accession) o);
        else if (o instanceof AnatomyItem)
            link = AnatomyItemPresentation.getLink((AnatomyItem) o, name);
        else if (o instanceof Publication)
            link = PublicationPresentation.getLink((Publication) o);
        else if (o instanceof Figure)
            link = FigurePresentation.getLink((Figure) o);
        else if (o instanceof OrthologySpecies)
            link = OrthologyPresentation.getLink((OrthologySpecies) o);
        else if (o instanceof Genotype)
            link = GenotypePresentation.getLink((Genotype) o);
        else if (o instanceof ExperimentCondition)
            link = ExperimentConditionPresentation.getLink((ExperimentCondition) o);
        else if (o instanceof GoTerm)
            link = GoTermPresentation.getLink((GoTerm) o);
        else if (o instanceof DevelopmentStage)
            link = DevelopmentStagePresentation.getLink((DevelopmentStage) o, longVersion);
        else if (o instanceof Organization)
            link = SourcePresentation.getLink((Organization) o);
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());
        return link;
    }

    private String createLinkStartFromSingleDomainObject(Object o) throws JspException {
        String linkStart;
        if (o instanceof Figure)
            linkStart = FigurePresentation.getLinkStartTag((Figure) o);
        else if (o instanceof Image)
            linkStart = ImagePresentation.getLinkStartTag((Image) o);
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());
        return linkStart;
    }

    private String createLinkEndFromSingleDomainObject(Object o) throws JspException {
        String linkEnd;
        if (o instanceof Figure)
            linkEnd = FigurePresentation.getLinkEndTag();
        else if (o instanceof Image)
            linkEnd = ImagePresentation.getLinkEndTag();
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());
        return linkEnd;
    }

    public int doEndTag() throws JspException {
        Object o = getEntity();
        StringBuilder linkBuffer = new StringBuilder();

        if (o == null)
            return Tag.SKIP_BODY;

        if (o instanceof Collection) {
            Collection collection = (Collection) o;
            int numberOfItems = collection.size();
            int index = 1;
            for (Object ob : collection) {
                linkBuffer.append(createLinkFromSingleDomainObject(ob));
                if (index < numberOfItems)
                    linkBuffer.append(", ");
                index++;
            }
        } else {
            BodyContent bc = getBodyContent();
            if (bc == null || StringUtils.isEmpty(bc.getString())) {
                //this method creates the entire <a href="..">...</a> tag using
                //the default link content
                linkBuffer.append(createLinkFromSingleDomainObject(o));
            } else {
                //if a body is passed in, it will replace the default link content
                linkBuffer.append(createLinkStartFromSingleDomainObject(o));
                linkBuffer.append(bc.getString());
                linkBuffer.append(createLinkEndFromSingleDomainObject(o));
            }


        }
        try {
            pageContext.getOut().print(linkBuffer.toString());
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client" + ioe.getMessage());
        }

        return Tag.EVAL_PAGE;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLongVersion() {
        return longVersion;
    }

    public void setLongVersion(boolean longVersion) {
        this.longVersion = longVersion;
    }
}
