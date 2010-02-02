package org.zfin.marker.presentation;

import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.people.Person;

/**
 */
public class MarkerBean {
    protected Marker marker ;
    private String zdbID;
    private MarkerExpression markerExpression;
    private RelatedMarkerDisplay markerRelationships ;
    protected int numPubs;
    private SummaryDBLinkDisplay proteinProductDBLinkDisplay;
    private MappedMarkerBean mappedMarkerBean;
    private SequenceInfo sequenceInfo;

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker ;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Person getUser() {
        return Person.getCurrentSecurityUser();
    }

    public MarkerExpression getMarkerExpression() {
        return markerExpression;
    }

    public void setMarkerExpression(MarkerExpression markerExpression) {
        this.markerExpression = markerExpression;
    }

    public RelatedMarkerDisplay getMarkerRelationships() {
        return markerRelationships;
    }

    public void setMarkerRelationships(RelatedMarkerDisplay markerRelationships) {
        this.markerRelationships = markerRelationships;
    }

    public int getNumPubs() {
        return numPubs;
    }

    public void setNumPubs(int numPubs) {
        this.numPubs = numPubs;
    }


    public SummaryDBLinkDisplay getProteinProductDBLinkDisplay() {
        return proteinProductDBLinkDisplay;
    }

    public void setProteinProductDBLinkDisplay(SummaryDBLinkDisplay proteinProductDBLinkDisplay) {
        this.proteinProductDBLinkDisplay = proteinProductDBLinkDisplay;
    }


    public MappedMarkerBean getMappedMarkerBean() {
        return mappedMarkerBean;
    }

    public void setMappedMarkerBean(MappedMarkerBean mappedMarkerBean) {
        this.mappedMarkerBean = mappedMarkerBean;
    }

    public SequenceInfo getSequenceInfo() {
        return sequenceInfo;
    }

    public void setSequenceInfo(SequenceInfo sequenceInfo) {
        this.sequenceInfo = sequenceInfo;
    }
}
