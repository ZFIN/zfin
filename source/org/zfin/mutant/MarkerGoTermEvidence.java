package org.zfin.mutant;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.gwt.root.dto.GoEvidenceQualifier;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.Date;
import java.util.Set;

/**
 */
public class MarkerGoTermEvidence {
    private String zdbID;
    private Marker marker;

    // this may need to be moved to its own
    private GoEvidenceCode evidenceCode;
    private GoEvidenceQualifier flag;
    private Publication source;
    private Term goTerm;
    private String note;

    // editing data
    private String createdBy;
    private Date createdWhen;
    private String modifiedBy;
    private Date modifiedWhen;

    private Set<InferenceGroupMember> inferredFrom;


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

    public Term getGoTerm() {
        return goTerm;
    }

    public void setGoTerm(Term goTerm) {
        this.goTerm = goTerm;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Date createdWhen) {
        this.createdWhen = createdWhen;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedWhen() {
        return modifiedWhen;
    }

    public void setModifiedWhen(Date modifiedWhen) {
        this.modifiedWhen = modifiedWhen;
    }

    public Set<InferenceGroupMember> getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(Set<InferenceGroupMember> inferredFrom) {
        this.inferredFrom = inferredFrom;
    }

    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
        if (!(o instanceof MarkerGoTermEvidence)) return false;

        MarkerGoTermEvidence that = (MarkerGoTermEvidence) o;

        // check the zdbID
        if(zdbID!=null && that.zdbID!=null){
            return zdbID.equals(that.zdbID) ;
        }

        if (evidenceCode != null ? !evidenceCode.equals(that.evidenceCode) : that.evidenceCode != null) return false;
        if (flag != that.flag) return false;
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
        result = 31 * result + (inferredFrom != null ? inferredFrom.hashCode() : 0);
        return result;
    }

    public boolean sameInferences(Set<InferenceGroupMember> inferredFrom1) {
        if(CollectionUtils.isEmpty(inferredFrom)  &&
                CollectionUtils.isEmpty(inferredFrom1)){
            return true ;
        }
        if( (inferredFrom!=null && inferredFrom1==null)
                || (inferredFrom==null && inferredFrom1!=null)){
            return false ;
        }

        if(inferredFrom.size()==inferredFrom1.size()){
            for(InferenceGroupMember inferenceGroupMember : inferredFrom){
                boolean hasMatchingInference = false ;
                for(InferenceGroupMember inferenceGroupMember1: inferredFrom1){
                    if(inferenceGroupMember.getInferredFrom().equals(inferenceGroupMember1.getInferredFrom())){
                        hasMatchingInference = true ;
                    }
                }
                if(false==hasMatchingInference){
                    return false ;
                }
            }
            return true ;
        }
        return false ;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MarkerGoTermEvidence");
        sb.append('{');
        sb.append("zdbID='").append(zdbID).append('\'');
        sb.append(", marker='").append(marker.getAbbreviation()).append('\'');
        sb.append(", evidenceCode='").append(evidenceCode.getName()).append('\'');
        sb.append(", flag='").append( (flag!=null ? flag.name() : "null ")).append('\'');
        sb.append(", source='").append( source.getZdbID()).append('\'');
        sb.append(", goTerm='").append( goTerm.getTermName()).append('\'');
        sb.append(", note='").append(note).append('\'');
        sb.append(", createdBy='").append(createdBy).append('\'');
        sb.append(", createdWhen=").append(createdWhen);
        sb.append(", modifiedBy='").append(modifiedBy).append('\'');
        sb.append(", modifiedWhen=").append(modifiedWhen);
        sb.append(", inferredFrom=");
        if(inferredFrom!=null){
            for(InferenceGroupMember inferenceGroupMember: inferredFrom){
                sb.append(inferenceGroupMember.getInferredFrom()).append(",");
            }
        }else{
            sb.append("none");
        }
        sb.append('}');
        return sb.toString();
    }

}
