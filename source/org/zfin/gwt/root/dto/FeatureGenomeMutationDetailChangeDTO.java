package org.zfin.gwt.root.dto;


import com.google.gwt.user.client.rpc.IsSerializable;


public class FeatureGenomeMutationDetailChangeDTO implements IsSerializable {

    private String zdbID;
    private String fgmdSeqVar ;
    private String fgmdSeqRef;

    public String getFgmdSeqVar() {
        return fgmdSeqVar;
    }

    public void setFgmdSeqVar(String fgmdSeqVar) {
        this.fgmdSeqVar = fgmdSeqVar;
    }

    public String getFgmdSeqRef() {
        return fgmdSeqRef;
    }

    public void setFgmdSeqRef(String fgmdSeqRef) {
        this.fgmdSeqRef = fgmdSeqRef;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}