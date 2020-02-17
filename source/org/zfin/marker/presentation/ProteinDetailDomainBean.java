package org.zfin.marker.presentation;

import org.zfin.expression.Figure;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.SortedSet;

/**
 */
public class ProteinDetailDomainBean {
    private String upID;
    private List<String> dbLinks;
    private ProteinDetail proDetail;

    public ProteinDetail getProDetail() {
        return proDetail;
    }

    public void setProDetail(ProteinDetail proDetail) {
        this.proDetail = proDetail;
    }

    public List<String> getDbLinks() {

        return dbLinks;
    }

    public void setDbLinks(List<String> dbLinks) {
        this.dbLinks = dbLinks;
    }

    private List<ProteinDomainRow> interProDomains;



    public List<ProteinDomainRow> getInterProDomains() {
        return interProDomains;
    }

    public void setInterProDomains(List<ProteinDomainRow> interProDomains) {
        this.interProDomains = interProDomains;
    }



    public String getUpID() {
        return upID;
    }

    public void setUpID(String upID) {
        this.upID = upID;
    }

}