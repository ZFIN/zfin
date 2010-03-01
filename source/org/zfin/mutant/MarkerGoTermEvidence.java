package org.zfin.mutant;

import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoFlagEnum;
import org.zfin.marker.Marker;
import org.zfin.ontology.GoTerm;
import org.zfin.publication.Publication;

import java.util.Date;
import java.util.Set;

/**
 */
public class MarkerGoTermEvidence {
    private String zdbID ;
    private Marker marker ;

    // this may need to be moved to its own
    private GoEvidenceCode evidenceCode ;
    private GoFlagEnum flag;
    private Publication source ;
    private GoTerm goTerm ;
    private String note ;

    // editing data
    private String createdBy ;
    private Date createdWhen ;
    private String modifiedBy ;
    private Date modifiedWhen ;

    private Set<InferenceGroupMember> inferredFrom ;


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

    public GoFlagEnum getFlag() {
        return flag;
    }

    public void setFlag(GoFlagEnum flag) {
        this.flag = flag;
    }

    public Publication getSource() {
        return source;
    }

    public void setSource(Publication source) {
        this.source = source;
    }

    public GoTerm getGoTerm() {
        return goTerm;
    }

    public void setGoTerm(GoTerm goTerm) {
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
}
