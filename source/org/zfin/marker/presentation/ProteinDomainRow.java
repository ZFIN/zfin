package org.zfin.marker.presentation;

import org.zfin.marker.presentation.ProteinDomainValue;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;

import java.util.Collection;
import java.util.Set;
import java.util.Map;

/**
 */
public class ProteinDomainRow {
    private String upID;
    private ProteinDetail proDetail;

    public ProteinDetail getProDetail() {
        return proDetail;
    }

    public void setProDetail(ProteinDetail proDetail) {
        this.proDetail = proDetail;
    }

    private Map<String, String> interProDomain;

    public Map<String, String> getInterProDomain() {
        return interProDomain;
    }

    public void setInterProDomain(Map<String, String> interProDomain) {
        this.interProDomain = interProDomain;
    }

    public String getUpID() {
        return upID;
    }

    public void setUpID(String upID) {
        this.upID = upID;
    }


}
