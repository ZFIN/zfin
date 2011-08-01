package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.gwt.root.dto.GoEvidenceQualifier;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class MarkerGoTermEvidence implements Comparable<MarkerGoTermEvidence> {
    private String zdbID;
    private Marker marker;

    // this may need to be moved to its own
    private GoEvidenceCode evidenceCode;
    private GoEvidenceQualifier flag;
    private Publication source;
    private GenericTerm goTerm;
    private String note;
    private Set<InferenceGroupMember> inferredFrom;

    // editing data

    /**
     * Curator created by.  May be null if comes from a null.
     */
    private Person createdBy;
    /**
     * Date record originally curated. For imported annotations this can be a date earlier than the external load date.
     */
    private Date createdWhen;
    /**
     * Curator who modified annotation during curation.  May be null if never modified.
     */
    private Person modifiedBy;
    /**
     * Date curator modified annotation during curation.  May be null if never modified.
     */
    private Date modifiedWhen;
    /**
     * Date external load brought in annotation.  This may be null if this was a curated record.
     */
    private Date externalLoadDate;
    /**
     * Organization that created the original record.  If we pull in the record from an external organization,
     * they may have pulled their record from somewhere else, as well.  This is that record.
     */
    private String organizationCreatedBy;
    /**
     * Organization that housed the original record.  This is the organization responsible for housing that record.
     */
    private GafOrganization gafOrganization;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public GoEvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(GoEvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public GoEvidenceQualifier getFlag() {
        return flag;
    }

    public void setFlag(GoEvidenceQualifier flag) {
        this.flag = flag;
    }

    public Publication getSource() {
        return source;
    }

    public void setSource(Publication source) {
        this.source = source;
    }

    public GenericTerm getGoTerm() {
        return goTerm;
    }

    public void setGoTerm(GenericTerm goTerm) {
        this.goTerm = goTerm;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }

    public Person getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Person modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Date createdWhen) {
        this.createdWhen = createdWhen;
    }


    public Date getModifiedWhen() {
        return modifiedWhen;
    }

    public void setModifiedWhen(Date modifiedWhen) {
        this.modifiedWhen = modifiedWhen;
    }

    public Date getExternalLoadDate() {
        return externalLoadDate;
    }

    public void setExternalLoadDate(Date externalLoadDate) {
        this.externalLoadDate = externalLoadDate;
    }

    public String getOrganizationCreatedBy() {
        return organizationCreatedBy;
    }

    public void setOrganizationCreatedBy(String organizationCreatedBy) {
        this.organizationCreatedBy = organizationCreatedBy;
    }

    public GafOrganization getGafOrganization() {
        return gafOrganization;
    }

    public void setGafOrganization(GafOrganization gafOrganization) {
        this.gafOrganization = gafOrganization;
    }

    public Set<InferenceGroupMember> getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(Set<InferenceGroupMember> inferredFrom) {
        this.inferredFrom = inferredFrom;
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
        if (inferredFrom != null ? !this.containsAllInferences(that) : that.inferredFrom != null) return false;

        return true;
    }


    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
        if (!(o instanceof MarkerGoTermEvidence)) return false;

        MarkerGoTermEvidence that = (MarkerGoTermEvidence) o;

        // check the zdbID
        if (zdbID != null && that.zdbID != null) {
            return zdbID.equals(that.zdbID);
        }

        if (evidenceCode != null ? !evidenceCode.equals(that.evidenceCode) : that.evidenceCode != null) return false;
        if (flag != that.flag) return false;
//        if (goTerm != null ? !goTerm.getZdbID().equals(that.goTerm.getZdbID()) : that.goTerm != null) return false;
        if (goTerm != null ? !goTerm.equals(that.goTerm) : that.goTerm != null) return false;
        if (marker != null ? !marker.equals(that.marker) : that.marker != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (inferredFrom != null ? !sameInferences(that.inferredFrom) : that.inferredFrom != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (marker != null ? marker.hashCode() : 0);
        result = 31 * result + (evidenceCode != null ? evidenceCode.hashCode() : 0);
        result = 31 * result + (flag != null ? flag.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (goTerm != null ? goTerm.hashCode() : 0);

        // have to compare the strings, since the inferences are generated for this and the key generates
        // a separate hash code
        result = 31 * result + (inferredFrom != null ? getInferencesAsString().hashCode() : 0);
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

    public Set<String> getInferencesAsString() {
        Set<String> theseInferences = new HashSet<String>();

        if (CollectionUtils.isNotEmpty(inferredFrom)) {
            for (InferenceGroupMember inferenceGroupMember : inferredFrom) {
                theseInferences.add(inferenceGroupMember.getInferredFrom());
            }
        }

        return theseInferences;

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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MarkerGoTermEvidence");
        sb.append('{');
        sb.append("zdbID='").append(zdbID).append('\'');
        sb.append(", marker='").append(marker.getAbbreviation()).append('\'');
        sb.append(", evidenceCode='").append(evidenceCode.getName()).append('\'');
        sb.append(", flag='").append((flag != null ? flag.name() : "null ")).append('\'');
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
