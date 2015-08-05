package org.zfin.framework.presentation.tags;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.DevelopmentStagePresentation;
import org.zfin.expression.*;
import org.zfin.expression.presentation.ExperimentConditionPresentation;
import org.zfin.expression.presentation.ExperimentPresentation;
import org.zfin.expression.presentation.ExpressionStatementPresentation;
import org.zfin.feature.Feature;
import org.zfin.feature.FeaturePrefix;
import org.zfin.feature.presentation.FeaturePrefixPresentation;
import org.zfin.feature.presentation.FeaturePresentation;
import org.zfin.fish.presentation.*;
import org.zfin.fish.FishAnnotation;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.framework.presentation.RunCandidatePresentation;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mapping.Panel;
import org.zfin.mapping.presentation.PanelPresentation;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.MarkerPresentation;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.FishExperimentPresentation;
import org.zfin.mutant.presentation.GenotypePresentation;
import org.zfin.mutant.presentation.PostComposedPresentationBean;
import org.zfin.mutant.repository.FeaturePresentationBean;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.ontology.TermExternalReference;
import org.zfin.ontology.presentation.*;
import org.zfin.orthology.OrthologySpecies;
import org.zfin.orthology.presentation.OrthologyPresentation;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.profile.presentation.ProfilePresentation;
import org.zfin.profile.presentation.SourcePresentation;
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

    private final Logger logger = Logger.getLogger(CreateLinkTag.class);

    private Object entity;
    private String name;
    private boolean longVersion;
    private boolean suppressPopupLink;
    private boolean curationLink;
    private boolean suppressMoDetails;

    public int doStartTag() throws JspException {
        return BodyTag.EVAL_BODY_BUFFERED;
    }

    private String createLinkFromSingleDomainObject(Object o) throws JspException {

        if (o == null) {
            logger.error("Tying to create a link with a null object:\n" + getBodyContent().getString());
            return "";
        }

        String link = null;
        if (o instanceof String) {
            Object entity = InfrastructureService.getEntityById((String) o);
            if (entity != null)
                o = entity;
            else
                link = (String) o;
        }
        if (o instanceof ProvidesLink) // provides a generic link interface
            link = ((ProvidesLink) o).getLink();
        else if (o instanceof SequenceTargetingReagent)
            link = MarkerPresentation.getLink((SequenceTargetingReagent) o, suppressPopupLink);
        else if (o instanceof Marker)  //handling of marker subtypes is taken care of in the getLink method
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
        else if (o instanceof PostComposedPresentationBean)
            link = TermPresentation.getLink((PostComposedPresentationBean) o, suppressPopupLink);
        else if (o instanceof Publication)
            link = PublicationPresentation.getLink((Publication) o);
        else if (o instanceof Figure)
            link = FigurePresentation.getLink((Figure) o);
        else if (o instanceof MartFish)
            link = FishPresentation.getLink((MartFish) o);
        else if (o instanceof Fish)
            link = FishPresentation.getLink((Fish) o, suppressPopupLink);
        else if (o instanceof FishAnnotation)
            link = FishAnnotationPresentation.getLink((FishAnnotation) o);
        else if (o instanceof Image)
            link = ImagePresentation.getLink((Image) o);
        else if (o instanceof Panel)
            link = PanelPresentation.getLink((Panel) o);
        else if (o instanceof OrthologySpecies)
            link = OrthologyPresentation.getLink((OrthologySpecies) o);
        else if (o instanceof DiseaseModel)
            link = DiseaseModelPresentation.getLink((DiseaseModel) o);
        else if (o instanceof Genotype)
            link = GenotypePresentation.getLink((Genotype) o, suppressPopupLink);
        else if (o instanceof Feature)
            link = FeaturePresentation.getLink((Feature) o);
        else if (o instanceof FeaturePrefix)
            link = FeaturePrefixPresentation.getLink((FeaturePrefix) o);
        else if (o instanceof FeaturePresentationBean)
            link = FeaturePresentation.getLink((FeaturePresentationBean) o);
        else if (o instanceof Experiment)
            link = ExperimentPresentation.getLink((Experiment) o, suppressPopupLink);
        else if (o instanceof FishExperiment)
            link = FishExperimentPresentation.getLink((FishExperiment) o, suppressPopupLink, suppressMoDetails);
        else if (o instanceof ExperimentCondition)
            link = ExperimentConditionPresentation.getLink((ExperimentCondition) o, suppressPopupLink);
        else if (o instanceof DevelopmentStage)
            link = DevelopmentStagePresentation.getLink((DevelopmentStage) o, longVersion);
        else if (o instanceof Organization)
            link = SourcePresentation.getLink((Organization) o);
        else if (o instanceof Term) {
            if (StringUtils.isEmpty(name))
                link = TermPresentation.getLink((Term) o, suppressPopupLink);
            else
                link = TermPresentation.getLink((Term) o, suppressPopupLink, name);

        } else if (o instanceof PostComposedEntity)
            link = TermPresentation.getLink((PostComposedEntity) o, suppressPopupLink);
        else if (o instanceof TermDTO)
            link = TermDTOPresentation.getLink((TermDTO) o);
        else if (o instanceof ExpressionResult)
            link = ExpressionResultPresentation.getLink((ExpressionResult) o, suppressPopupLink, curationLink);
        else if (o instanceof TermExternalReference)
            link = TermExternalReferencePresentation.getLink((TermExternalReference) o);
        else if (o instanceof ExpressionStatement)
            link = ExpressionStatementPresentation.getLink((ExpressionStatement) o, suppressPopupLink);
        else if (o instanceof PhenotypeStatement)
            link = PhenotypePresentation.getLink((PhenotypeStatement) o, suppressPopupLink, curationLink);
        else if (o instanceof ZfinEntity)
            link = ZfinEntityPresentation.getLink((ZfinEntity) o);
        else if (o instanceof EntityZdbID)
            link = "<a href='/" + ((EntityZdbID) o).getZdbID() + "'>" + ((EntityZdbID) o).getAbbreviation() + "</a>";
        else
            throw new JspException("Tag is not yet implemented for a class of type " + o.getClass());
        return link;
    }

    private String createLinkStartFromSingleDomainObject(Object o) throws JspException {
        String linkStart;
        if (o instanceof Figure)
            linkStart = FigurePresentation.getLinkStartTag((Figure) o);
        else if (o instanceof Organization)
            linkStart = ProfilePresentation.getLinkStartTag((Organization) o);
        else if (o instanceof Person)
            linkStart = ProfilePresentation.getLinkStartTag((Person) o);
        else if (o instanceof Image)
            linkStart = ImagePresentation.getLinkStartTag((Image) o);
        else if (o instanceof Publication)
            linkStart = PublicationPresentation.getLinkStartTag((Publication) o);
        else if (o instanceof Term)
            linkStart = TermPresentation.getLinkStartTag((Term) o);
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
        else if (o instanceof Publication)
            linkEnd = PublicationPresentation.getLinkEndTag();
        else if (o instanceof Person || o instanceof Organization)
            linkEnd = ProfilePresentation.getLinkEndTag();
        else if (o instanceof Term)
            linkEnd = TermPresentation.getLinkEndTag((Term) o);
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

    public boolean isSuppressPopupLink() {
        return suppressPopupLink;
    }

    public void setSuppressPopupLink(boolean suppressPopupLink) {
        this.suppressPopupLink = suppressPopupLink;
    }

    public boolean isCurationLink() {
        return curationLink;
    }

    public void setCurationLink(boolean curationLink) {
        this.curationLink = curationLink;
    }

    public boolean isSuppressMoDetails() {
        return suppressMoDetails;
    }

    public void setSuppressMoDetails(boolean suppressMoDetails) {
        this.suppressMoDetails = suppressMoDetails;
    }
}
