package org.zfin.marker.presentation;

import org.zfin.genomebrowser.presentation.GenomeBrowserImage;
import org.zfin.marker.Clone;
import org.zfin.sequence.MarkerDBLinkList;
import org.zfin.sequence.ReferenceDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class CloneBean extends MarkerBean {

    // list
    //protected List<MarkerRelationship> markerRelationships;
    private SequenceInfo sequenceInfo;
    private int numPubs;
    private MarkerDBLinkList otherTranscripts;
    private boolean isThisseProbe;
    private SummaryDBLinkDisplay summaryDBLinkDisplay;
    private List<OrganizationLink> suppliers;
    private GenomeBrowserImage image;

    public String getDbSnps() {
        return dbSnps;
    }

    public void setDbSnps(String dbSnps) {
        this.dbSnps = dbSnps;
    }

    private String dbSnps;

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

    public SummaryDBLinkDisplay getSummaryDBLinkDisplay() {
        return summaryDBLinkDisplay;
    }

    public void setSummaryDBLinkDisplay(SummaryDBLinkDisplay summaryDBLinkDisplay) {
        this.summaryDBLinkDisplay = summaryDBLinkDisplay;
    }

    public void addFakePubs(ReferenceDatabase ensemblDatabase) {

        if(hasRealPubs(ensemblDatabase)) return ;

        List<LinkDisplay> fakeLinks = new ArrayList<LinkDisplay>();
        for(LinkDisplay linkDisplay: getOtherMarkerPages()){
            if(linkDisplay.getReferenceDatabaseName().startsWith("VEGA_")){
                LinkDisplay fakeLinkDisplay = new LinkDisplay();
                fakeLinkDisplay.setAccession(linkDisplay.getAccession());
                fakeLinkDisplay.setMarkerZdbID(linkDisplay.getMarkerZdbID());
                fakeLinkDisplay.setReferences(linkDisplay.getReferences());
                fakeLinkDisplay.setReferenceDatabaseName(ensemblDatabase.getForeignDB().getDbName().toString());
                fakeLinkDisplay.setUrlPrefix(ensemblDatabase.getBaseURL());
                fakeLinks.add(fakeLinkDisplay);
            }
        }
        getOtherMarkerPages().addAll(fakeLinks);
    }

    private boolean hasRealPubs(ReferenceDatabase ensemblDatabase) {
        for(LinkDisplay linkDisplay: getOtherMarkerPages()){
            if(linkDisplay.getReferenceDatabaseName().startsWith(ensemblDatabase.getForeignDB().getDbName().toString())){
                return true;
            }
        }
        return false;
    }

    public List<OrganizationLink> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<OrganizationLink> suppliers) {
        this.suppliers = suppliers;
    }

    public GenomeBrowserImage getImage() {
        return image;
    }

    public void setImage(GenomeBrowserImage image) {
        this.image = image;
    }
}