package org.zfin.antibody.presentation;

import org.zfin.ExternalNote;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyService;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.service.MarkerService;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class AntibodyBean extends PublicationListBean {

    protected Antibody antibody;

    protected ExpressionSummaryCriteria expressionSummaryCriteria;

    protected Term superTerm;
    protected Term subTerm;
    protected DevelopmentStage startStage;

    protected DevelopmentStage endStage;

    protected AntibodyService antibodyStat;

    private boolean onlyFiguresWithImg;

    private boolean update = false;
    private boolean addPublication = false;

    private String antibodyNewPubZdbID;

    public static final String AB_NEWPUB_ZDB_ID = "antibodyNewPubZdbID";

    public Antibody getAntibody() {
        if (antibody == null) {
            antibody = new Antibody();
        }
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public Term getSuperTerm() {
        if (superTerm == null) {
            superTerm = new GenericTerm();
        }
        return superTerm;
    }

    public void setSuperTerm(Term superTerm) {
        this.superTerm = superTerm;
    }

    public Term getSubTerm() {
        if (subTerm == null) {
            subTerm = new GenericTerm();
        }
        return subTerm;
    }

    public void setSubTerm(Term subTerm) {
        this.subTerm = subTerm;
    }

    public DevelopmentStage getStartStage() {
        if (startStage == null) {
            startStage = new DevelopmentStage();
        }
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        if (endStage == null) {
            endStage = new DevelopmentStage();
        }
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public AntibodyService getAntibodyStat() {
        if (antibodyStat == null) {
            if (antibody == null)
                return null;
            antibodyStat = new AntibodyService(antibody);
        }

        return antibodyStat;
    }

    public void setAntibodyStat(AntibodyService antibodyStat) {
        this.antibodyStat = antibodyStat;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(antibody.getZdbID());
    }

    public SortedSet<ExternalNote> getNotesSortedByPubTime() {
        if (antibody == null) {
            return new TreeSet<ExternalNote>();
        }
        return new TreeSet<ExternalNote>(antibody.getExternalNotes());
    }

    public ExpressionSummaryCriteria getExpressionSummaryCriteria() {
        return expressionSummaryCriteria;
    }

    public void setExpressionSummaryCriteria(ExpressionSummaryCriteria expressionSummaryCriteria) {
        this.expressionSummaryCriteria = expressionSummaryCriteria;
    }

    public Set<Publication> getPublications() {
        Set<Publication> publications = new HashSet<Publication>();
        if (antibody == null)
            return publications;

        Set<PublicationAttribution> pubAttributions = antibody.getPublications();
        if (pubAttributions != null && !pubAttributions.isEmpty()) {
            for (PublicationAttribution attr : pubAttributions) {
                Publication pub = attr.getPublication();
                if (pub != null)
                    publications.add(pub);
            }
        }

        return publications;
    }

    public boolean isUpdate() {
        return update;
    }


    public void setUpdate(boolean update) {
        this.update = update;
    }

    public int getNumOfAttributesPublications() {
        return getAntibodyAttributesPublications().size();
    }

    public Set<Publication> getAntibodyAttributesPublications() {
        Set<Publication> publications = new HashSet<Publication>();

        if (antibody == null)
            return publications;

        // add alias associated publications
        for (Publication pub : MarkerService.getAliasAttributions(antibody)) {
            publications.add(pub);
        }


        AntibodyService antibodyData = getAntibodyStat();

        if (antibodyData != null) {

            // add antigen-gene associated publications
            Set<MarkerRelationship> relationships = antibodyData.getSortedAntigenRelationships();
            if (relationships != null && !relationships.isEmpty()) {
                for (MarkerRelationship mrkrRel : relationships) {
                    Set<PublicationAttribution> antigenPubs = mrkrRel.getPublications();
                    if (antigenPubs != null && !antigenPubs.isEmpty()) {
                        for (PublicationAttribution pubAttr : antigenPubs)
                            publications.add(pubAttr.getPublication());
                    }
                }
            }
        }

        // add labeling associated publications
        List<AnatomyLabel> labelings = getAntibodyStat().getAntibodyLabelings();
        if (labelings != null && !labelings.isEmpty()) {
            for (AnatomyLabel labeling : labelings) {
                Set<Publication> labelingPubs = labeling.getPublications();
                if (labelingPubs != null && !labelingPubs.isEmpty())
                    publications.addAll(labelingPubs);
            }
        }

        // add note associated publications
        SortedSet<ExternalNote> notes = getNotesSortedByPubTime();
        if (notes != null && !notes.isEmpty()) {
            for (ExternalNote note : notes) {
                Set<PublicationAttribution> notePubs = note.getPubAttributions();
                if (notePubs != null && !notePubs.isEmpty()) {
                    for (PublicationAttribution pubAttr : notePubs)
                        publications.add(pubAttr.getPublication());
                }
            }
        }

        return publications;
    }

    public List<Publication> getSortedPublishedPublications() {
        List<Publication> publishedPublications = new ArrayList<Publication>();
        Set<Publication> AntibodyPublications = getAntibodyAttributesPublications();
        for (Publication pub : getSortedPublications()) {
            if (!pub.isUnpublished()) {
                if (update) {
                    if (AntibodyPublications.contains(pub))
                        pub.setDeletable(false);
                    else
                        pub.setDeletable(true);
                }
                publishedPublications.add(pub);
            }
        }
        sortPublications(publishedPublications, getOrderBy());
        return publishedPublications;
    }

    public List<Publication> getSortedUnpublishedPublications() {
        List<Publication> unpublishedPublications = new ArrayList<Publication>();
        Set<Publication> AntibodyPublications = getAntibodyAttributesPublications();
        for (Publication pub : getSortedPublications()) {
            if (pub.isUnpublished()) {
                String yearInAuthors = "(" + pub.getYear() + ")";
                String authors = pub.getAuthors();
                pub.setAuthors(authors.replace(yearInAuthors, ""));
                if (update) {
                    if (AntibodyPublications.contains(pub))
                        pub.setDeletable(false);
                    else
                        pub.setDeletable(true);
                }
                unpublishedPublications.add(pub);
            }
        }
        sortPublications(unpublishedPublications, getOrderBy());
        return unpublishedPublications;
    }

    public boolean isOnlyFiguresWithImg() {
        return onlyFiguresWithImg;
    }

    public void setOnlyFiguresWithImg(boolean onlyFiguresWithImg) {
        this.onlyFiguresWithImg = onlyFiguresWithImg;
    }

    public boolean isAddPublication() {
        return addPublication;
    }

    public void setAddPublication(boolean addPublication) {
        this.addPublication = addPublication;
    }

    public String getAntibodyNewPubZdbID() {
        return antibodyNewPubZdbID;
    }

    public void setAntibodyNewPubZdbID(String antibodyNewPubZdbID) {
        this.antibodyNewPubZdbID = antibodyNewPubZdbID;
    }

    public int getNumOfUsageNotes() {
        if (antibody.getExternalNotes() == null)
            return 0;
        else
            return getNotesSortedByPubTime().size();
    }

    public String getEditURL() {
        String zdbID = antibody.getZdbID();
//        return "/action/antibody/update-details?antibody.zdbID=" + zdbID;
        return "/action/marker/marker-edit?zdbID=" + zdbID;
    }

    public String getDeleteURL() {
        String zdbID = antibody.getZdbID();
        return "/action/marker/delete?zdbIDToDelete=" + zdbID;
    }

    public String getMergeURL() {
        String zdbID = antibody.getZdbID();
        return "/action/marker/merge?zdbIDToDelete=" + zdbID;
    }

    /*
    public int getNumOfSuppliers() {
        if (antibody == null || antibody.getSuppliers() == null)
          return 0;
        else
          return antibody.getSuppliers().size();
    }
    */
}
