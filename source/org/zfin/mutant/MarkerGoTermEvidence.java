package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.StringEnumValueUserType;
import org.zfin.gwt.root.dto.GoEvidenceQualifier;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "marker_go_term_evidence")
public class MarkerGoTermEvidence implements Comparable<MarkerGoTermEvidence> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MarkerGoTermEvidence")
    @GenericGenerator(name = "MarkerGoTermEvidence",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "MRKRGOEV"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "mrkrgoev_zdb_id")
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "mrkrgoev_mrkr_zdb_id", nullable = false)
    private Marker marker;

    // this may need to be moved to its own
    @ManyToOne
    @JoinColumn(name = "mrkrgoev_evidence_code", nullable = false)
    private GoEvidenceCode evidenceCode;

    @Column(name = "mrkrgoev_gflag_name")
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@Parameter(name = "enumClassname",
                    value = "org.zfin.gwt.root.dto.GoEvidenceQualifier")})
    private GoEvidenceQualifier flag;

    @ManyToOne
    @JoinColumn(name = "mrkrgoev_source_zdb_id", nullable = false)
    private Publication source;

    @ManyToOne
    @JoinColumn(name = "mrkrgoev_term_zdb_id", nullable = false)
    private GenericTerm goTerm;

    @ManyToOne
    @JoinColumn(name = "mrkrgoev_relation_term_zdb_id")
    private GenericTerm qualifierRelation;

    @Column(name = "mrkrgoev_notes")
    private String note;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "infgrmem_mrkrgoev_zdb_id", referencedColumnName = "mrkrgoev_zdb_id", insertable = false, updatable = false)
    private Set<InferenceGroupMember> inferredFrom;

    @OneToMany(mappedBy = "mgtaegMarkerGoEvidence", fetch = FetchType.EAGER)
    private Set<MarkerGoTermAnnotationExtnGroup> goTermAnnotationExtnGroup;

    @Column(name = "mrkrgoev_protein_accession")
    private String geneProductAccession;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "noctua_model_annotation",
            joinColumns = @JoinColumn(name = "nma_mrkrgoev_zdb_id"),
            inverseJoinColumns = @JoinColumn(name = "nma_nm_id"))
    private Set<NoctuaModel> noctuaModels;

    // editing data

    /**
     * Curator created by.  May be null if comes from a null.
     */
    @ManyToOne
    @JoinColumn(name = "mrkrgoev_contributed_by")
    private Person createdBy;

    /**
     * Date record originally curated. For imported annotations this can be a date earlier than the external load date.
     */
    @Column(name = "mrkrgoev_date_entered", nullable = false)
    private Date createdWhen;

    /**
     * Curator who modified annotation during curation.  May be null if never modified.
     */
    @ManyToOne
    @JoinColumn(name = "mrkrgoev_modified_by")
    private Person modifiedBy;

    /**
     * Date curator modified annotation during curation.  May be null if never modified.
     */
    @Column(name = "mrkrgoev_date_modified", nullable = false)
    private Date modifiedWhen;

    /**
     * Date external load brought in annotation.  This may be null if this was a curated record.
     */
    @Column(name = "mrkrgoev_external_load_date")
    private Date externalLoadDate;

    /**
     * Organization that created the original record.  If we pull in the record from an external organization,
     * they may have pulled their record from somewhere else, as well.  This is that record.
     */
    @Column(name = "mrkrgoev_annotation_organization_created_by", nullable = false)
    private String organizationCreatedBy;

    /**
     * Organization that housed the original record.  This is the organization responsible for housing that record.
     */
    @ManyToOne
    @JoinColumn(name = "mrkrgoev_annotation_organization", nullable = false)
    private GafOrganization gafOrganization;


    public void addGoTermAnnotationExtnGroup(MarkerGoTermAnnotationExtnGroup goTermAnnotationExtnGroup) {
        if (this.goTermAnnotationExtnGroup == null)
            this.goTermAnnotationExtnGroup = new HashSet<>();
        this.goTermAnnotationExtnGroup.add(goTermAnnotationExtnGroup);
    }

    public String getNoctuaModelId() {

        if (CollectionUtils.isEmpty(noctuaModels))
            return null;
        return noctuaModels.iterator().next().getId();
    }

    /**
     * @param that
     * @return If this is more specific / equal go term and has all of that incoming inferences than true.
     */
//    public boolean isMoreSpecificAnnotation(MarkerGoTermEvidence that, String... relationshipTypes){
    public boolean isSameButGo(MarkerGoTermEvidence that) {
        if (!evidenceCode.equals(that.evidenceCode)) return false;
        if (!marker.equals(that.marker)) return false;
        if (!source.equals(that.source)) return false;
        if (flag != that.flag) return false;
        if (qualifierRelation != that.qualifierRelation) return false;
        if (inferredFrom != null ? !this.containsAllInferences(that) : that.inferredFrom != null) return false;
        if ((CollectionUtils.isNotEmpty(goTermAnnotationExtnGroup) && CollectionUtils.isEmpty(that.getAnnotationExtensions())) ||
                (CollectionUtils.isEmpty(goTermAnnotationExtnGroup) && CollectionUtils.isNotEmpty(that.getAnnotationExtensions())))
            return false;
        if (CollectionUtils.isNotEmpty(goTermAnnotationExtnGroup) && !this.containsAllAnnotationExtensions(that))
            return false;

        if (!StringUtils.equals(geneProductAccession, that.geneProductAccession)) return false;


        return true;
    }


    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
        if (!(o instanceof MarkerGoTermEvidence that)) return false;

        // check the zdbID
        if (zdbID != null && that.zdbID != null) {
            return zdbID.equals(that.zdbID);
        }

        if (evidenceCode != null ? !evidenceCode.equals(that.evidenceCode) : that.evidenceCode != null) return false;
        if (flag != that.flag) return false;
        if (qualifierRelation != that.qualifierRelation) return false;
//        if (goTerm != null ? !goTerm.getZdbID().equals(that.goTerm.getZdbID()) : that.goTerm != null) return false;
        if (goTerm != null ? !goTerm.equals(that.goTerm) : that.goTerm != null) return false;
        if (marker != null ? !marker.equals(that.marker) : that.marker != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (geneProductAccession != null ? !geneProductAccession.equals(that.geneProductAccession) : that.geneProductAccession != null)
            return false;
        if (inferredFrom != null ? !sameInferences(that.inferredFrom) : that.inferredFrom != null) return false;
//return true;
        return CollectionUtils.isEmpty(getAnnotationExtensions()) ? CollectionUtils.isEmpty(that.getAnnotationExtensions()) :
                sameAnnotationExtension(that.getAnnotationExtensions());
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (marker != null ? marker.hashCode() : 0);
        result = 31 * result + (evidenceCode != null ? evidenceCode.hashCode() : 0);
        result = 31 * result + (flag != null ? flag.hashCode() : 0);
        result = 31 * result + (qualifierRelation != null ? qualifierRelation.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (geneProductAccession != null ? geneProductAccession.hashCode() : 0);
        result = 31 * result + (goTerm != null ? goTerm.hashCode() : 0);

        // have to compare the strings, since the inferences are generated for this and the key generates
        // a separate hash code
        result = 31 * result + (inferredFrom != null ? getInferencesAsString().hashCode() : 0);
        result = 31 * result + (getAnnotationExtensions() != null ? getAnnotationExtensions().hashCode() : 0);
        return result;
    }

    public boolean sameInferences(Set<InferenceGroupMember> inferredFrom1) {
        if (CollectionUtils.isEmpty(inferredFrom) &&
                CollectionUtils.isEmpty(inferredFrom1)) {
            return true;
        }
        if ((inferredFrom != null && inferredFrom1 == null)
                || (inferredFrom == null && inferredFrom1 != null)) {
            return false;
        }

        if (inferredFrom.size() == inferredFrom1.size()) {
            for (InferenceGroupMember inferenceGroupMember : inferredFrom) {
                boolean hasMatchingInference = false;
                for (InferenceGroupMember inferenceGroupMember1 : inferredFrom1) {
                    if (inferenceGroupMember.getInferredFrom().equals(inferenceGroupMember1.getInferredFrom())) {
                        hasMatchingInference = true;
                    }
                }
                if (false == hasMatchingInference) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean sameAnnotationExtension(Set<MarkerGoTermAnnotationExtn> annotExtn1) {
        if (CollectionUtils.isEmpty(getAnnotationExtensions()) &&
                CollectionUtils.isEmpty(annotExtn1)) {
            return true;
        }
        if ((getAnnotationExtensions() != null && annotExtn1 == null)
                || (getAnnotationExtensions() == null && annotExtn1 != null)) {
            return false;
        }

        if (getAnnotationExtensions().size() == annotExtn1.size()) {
            for (MarkerGoTermAnnotationExtn mgtae : getAnnotationExtensions()) {
                boolean hasMatchingAnnotExtn = false;
                for (MarkerGoTermAnnotationExtn mgannotExtn1 : annotExtn1) {
                    if (mgtae.getRelationshipTerm().equals(mgannotExtn1.getRelationshipTerm())) {
                        if (mgtae.getIdentifierTermText().equals(mgannotExtn1.getIdentifierTermText()))
                            hasMatchingAnnotExtn = true;
                    }
                }
                if (false == hasMatchingAnnotExtn) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public Set<String> getInferencesAsString() {
        Set<String> theseInferences = new HashSet<String>();

        if (CollectionUtils.isNotEmpty(inferredFrom)) {
            for (InferenceGroupMember inferenceGroupMember : inferredFrom) {
                theseInferences.add(inferenceGroupMember.getInferredFrom());
            }
        }

        return theseInferences;

    }

    public Set<MarkerGoTermAnnotationExtn> getAnnotationExtensions() {
        Set<MarkerGoTermAnnotationExtn> theseAnnotExtns = new HashSet<>();

        if (CollectionUtils.isNotEmpty(goTermAnnotationExtnGroup)) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeGroup : goTermAnnotationExtnGroup) {
                if (CollectionUtils.isNotEmpty(mgtaeGroup.getMgtAnnoExtns())) {
                    for (MarkerGoTermAnnotationExtn mgtae : mgtaeGroup.getMgtAnnoExtns()) {
                        theseAnnotExtns.add(mgtae);
                    }
                }
            }
        }
        return theseAnnotExtns;
    }


    /**
     * Determine if ALL of these inferences are contained in the inferences on this object.
     *
     * @param markerGoTermEvidence
     * @return All inferences must be in this.inferredFrom
     */
    public boolean containsAllInferences(MarkerGoTermEvidence markerGoTermEvidence) {
        Collection<String> thatInferences = markerGoTermEvidence.getInferencesAsString();
        Collection<String> theseInferences = getInferencesAsString();


        // if the interstion contains all of the original elements, then it should be the same size as the original
        return CollectionUtils.intersection(thatInferences, theseInferences).size() == thatInferences.size();
    }

    /**
     * Determine if ALL of these annotation extensions are contained in the annotations extensions on this object.
     *
     * @param markerGoTermEvidence
     * @return All annotations must be in this.annotationExtns
     */
    public boolean containsAllAnnotationExtensions(MarkerGoTermEvidence markerGoTermEvidence) {
        Collection<MarkerGoTermAnnotationExtn> thatAnnotExtns = markerGoTermEvidence.getAnnotationExtensions();
        Collection<MarkerGoTermAnnotationExtn> theseAnnotExtns = getAnnotationExtensions();


        // if the intersection contains all of the original elements, then it should be the same size as the original
        return CollectionUtils.intersection(thatAnnotExtns, theseAnnotExtns).size() == thatAnnotExtns.size();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MarkerGoTermEvidence");
        sb.append('{');
        sb.append("zdbID='").append(zdbID).append('\'');
        sb.append(", marker='").append(marker.getAbbreviation()).append('\'');
        sb.append(", evidenceCode='").append(evidenceCode.getName()).append('\'');
        sb.append(", flag='").append((flag != null ? flag.name() : "null ")).append('\'');
        sb.append(", qualifierRelation='").append((qualifierRelation != null ? qualifierRelation.getTermName() : "null ")).append('\'');
        sb.append(", source='").append(source.getZdbID()).append('\'');
        sb.append(", goTerm='").append(goTerm.getTermName()).append('\'');
        sb.append(", note='").append(note).append('\'');
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", createdWhen=").append(createdWhen);
        sb.append(", modifiedBy='").append(modifiedBy).append('\'');
        sb.append(", modifiedWhen=").append(modifiedWhen);
        sb.append(", organizationCreatedBy=").append(organizationCreatedBy);
        sb.append(", externalLoadDate=").append(externalLoadDate);
        sb.append(", inferredFrom=");
        if (inferredFrom != null) {
            for (InferenceGroupMember inferenceGroupMember : inferredFrom) {
                sb.append(inferenceGroupMember.getInferredFrom()).append(",");
            }
        } else {
            sb.append("none");
        }
        sb.append(",annotationExtns=");
        if (goTermAnnotationExtnGroup != null) {
            for (MarkerGoTermAnnotationExtnGroup mgtaeg : goTermAnnotationExtnGroup) {
                for (MarkerGoTermAnnotationExtn mgtae : mgtaeg.getMgtAnnoExtns()) {
                    sb.append(mgtae.getRelationshipTerm() + '(' + mgtae.getIdentifierTermText() + ')').append(",");
                }
            }
        } else {
            sb.append("none");
        }
        sb.append(", geneProductFormID=");
        if (geneProductAccession != null) {
            sb.append(geneProductAccession);
        } else {
            sb.append("none");
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(MarkerGoTermEvidence o) {
        String nameOne = getMarker().getAbbreviation();
        String nameTwo = o.getMarker().getAbbreviation();
        if (!nameOne.equals(nameTwo))
            return nameOne.compareTo(nameTwo);
        return 0;
    }
}
