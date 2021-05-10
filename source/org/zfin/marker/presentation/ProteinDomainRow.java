package org.zfin.marker.presentation;

import org.zfin.marker.presentation.ProteinDomainValue;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.*;

/**
 */
public class ProteinDomainRow {
    private String upID;
    private ProteinDetail proDetail;
    private DBLink proDBLink;
    private Boolean isPDB;

    public Boolean getPDB() {
        return isPDB;
    }

    public void setPDB(Boolean PDB) {
        isPDB = PDB;
    }

    public DBLink getProDBLink() {
        return proDBLink;
    }

    public void setProDBLink(DBLink proDBLink) {
        this.proDBLink = proDBLink;
    }

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
