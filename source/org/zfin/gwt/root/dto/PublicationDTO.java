package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class PublicationDTO implements IsSerializable {
    private String title ;
    private String authors ;
    private String zdbID ;
    private String abstractText ;
    private String doi ;
    private String accession;
    private String citation ;
    private String miniRef;

    public PublicationDTO(){}

    public PublicationDTO(String title,String zdbID){
        this.title = title ;
        this.zdbID = zdbID ; 
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getMiniRef() {
        return miniRef;
    }

    public void setMiniRef(String miniRef) {
        this.miniRef = miniRef;
    }
}
