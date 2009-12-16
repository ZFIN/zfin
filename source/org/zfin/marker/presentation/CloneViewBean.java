package org.zfin.marker.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Clone;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLinkList;

/**
 */
public class CloneViewBean extends MarkerBean {

    // list
    //protected List<MarkerRelationship> markerRelationships;
    private SequenceInfo sequenceInfo;
    private int numPubs;
    private MarkerDBLinkList otherTranscripts;
    private boolean isThisseProbe;
    private MarkerExpression markerExpression;
    private RelatedMarkerDisplay markerRelationships;
    private MappedMarkerBean mappedMarkerBean;
    private SummaryDBLinkDisplay summaryDBLinkDisplay;


    public RelatedMarkerDisplay getMarkerRelationships() {
        return markerRelationships;
    }

    public void setMarkerRelationships(RelatedMarkerDisplay markerRelationships) {
        this.markerRelationships = markerRelationships;
    }

    public SequenceInfo getSequenceInfo() {
        return sequenceInfo;
    }

    public void setSequenceInfo(SequenceInfo sequenceInfo) {
        this.sequenceInfo = sequenceInfo;
    }

    public int getNumPubs() {
        return numPubs;
    }

    public void setNumPubs(int numPubs) {
        this.numPubs = numPubs;
    }

    public MarkerDBLinkList getOtherTranscripts() {
        return otherTranscripts;
    }

    public void setOtherTranscripts(MarkerDBLinkList otherTranscripts) {
        this.otherTranscripts = otherTranscripts;
    }

    public Clone getClone() {
        return (Clone) marker;
    }

    public void setClone(Clone clone) {
        this.marker = clone;
    }

    public boolean isThisseProbe() {
        return isThisseProbe;
    }

    public void setThisseProbe(boolean thisseProbe) {
        isThisseProbe = thisseProbe;
    }

    public MarkerExpression getMarkerExpression() {
        return markerExpression;
    }

    public void setMarkerExpression(MarkerExpression markerExpression) {
        this.markerExpression = markerExpression;
    }

    public SummaryDBLinkDisplay getSummaryDBLinkDisplay() {
        return summaryDBLinkDisplay;
    }

    public void setSummaryDBLinkDisplay(SummaryDBLinkDisplay summaryDBLinkDisplay) {
        this.summaryDBLinkDisplay = summaryDBLinkDisplay;
    }

    public MappedMarkerBean getMappedMarkerBean() {
        return mappedMarkerBean;
    }

    public void setMappedMarkerBean(MappedMarkerBean mappedMarkerBean) {
        this.mappedMarkerBean = mappedMarkerBean;
    }

    public String getDeleteURL() {
        String zdbID = getClone().getZdbID();
        return "/" + ZfinProperties.getWebDriver() + "?MIval=aa-delete_record.apg&rtype=marker&OID=" + zdbID;
    }

    public String getEditURL() {
        String zdbID = getClone().getZdbID();
        return "/action/marker/marker-edit?zdbID=" + zdbID;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(marker.getZdbID());
    }


}