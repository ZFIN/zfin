package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.api.View;

import java.util.List;

/**
 */
public class PublicationDTO implements IsSerializable {

    @JsonView(View.API.class)
    private String title;
    private String authors;
    private List<PersonDTO> registeredAuthors;
    @JsonView(View.API.class)
    private String zdbID;
    private String abstractText;
    private String doi;
    private Integer accession;
    private String citation;
    @JsonView(View.API.class)
    private String miniRef;

    public PublicationDTO() {
    }

    public PublicationDTO(PubEnum pubEnum) {
        this.title = pubEnum.title();
        this.zdbID = pubEnum.zdbID();
    }

    public PublicationDTO(String title, String zdbID) {
        this.title = title;
        this.zdbID = zdbID;
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

    public List<PersonDTO> getRegisteredAuthors() {
        return registeredAuthors;
    }

    public void setRegisteredAuthors(List<PersonDTO> registeredAuthors) {
        this.registeredAuthors = registeredAuthors;
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

    public Integer getAccession() {
        return accession;
    }

    public void setAccession(Integer accession) {
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
