package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.GregorianCalendar;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ReferenceDTO extends ZfinDTO {

    private String primaryId;
    private String title;
    private GregorianCalendar datePublished;
    private String citation;
    private String allianceCategory;
    private GregorianCalendar dateArrivedInPubMed;
    private GregorianCalendar dateLastModified;
    private String volume;
    private String pages;
    private List<String> keywords;
    private List<String> pubMedType;
    private String publisher;
    private List<MODReferenceTypeDTO> MODReferenceTypes;
    private List<ReferenceTagDTO> tags;
    private List<MESHDetailDTO> meshTerms;
    private List<CrossReferenceDTO> crossReferences;
    private String resourceAbbreviation;
    private List<AuthorReferenceDTO> authors;
    private String abstractText;

    @Override
    public String getPrimaryId() {
        return primaryId;
    }

    @Override
    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GregorianCalendar getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(GregorianCalendar datePublished) {
        this.datePublished = datePublished;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getAllianceCategory() {
        return allianceCategory;
    }

    public void setAllianceCategory(String allianceCategory) {
        this.allianceCategory = allianceCategory;
    }

    public GregorianCalendar getDateArrivedInPubMed() {
        return dateArrivedInPubMed;
    }

    public void setDateArrivedInPubMed(GregorianCalendar dateArrivedInPubMed) {
        this.dateArrivedInPubMed = dateArrivedInPubMed;
    }

    public GregorianCalendar getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(GregorianCalendar dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getPubMedType() {
        return pubMedType;
    }

    public void setPubMedType(List<String> pubMedType) {
        this.pubMedType = pubMedType;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public List<MODReferenceTypeDTO> getMODReferenceTypes() {
        return MODReferenceTypes;
    }

    public void setMODReferenceTypes(List<MODReferenceTypeDTO> MODReferenceTypes) {
        this.MODReferenceTypes = MODReferenceTypes;
    }

    public List<ReferenceTagDTO> getTags() {
        return tags;
    }

    public void setTags(List<ReferenceTagDTO> tags) {
        this.tags = tags;
    }

    public List<MESHDetailDTO> getMeshTerms() {
        return meshTerms;
    }

    public void setMeshTerms(List<MESHDetailDTO> meshTerms) {
        this.meshTerms = meshTerms;
    }

    public List<CrossReferenceDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public String getResourceAbbreviation() {
        return resourceAbbreviation;
    }

    public void setResourceAbbreviation(String resourceAbbreviation) {
        this.resourceAbbreviation = resourceAbbreviation;
    }

    public List<AuthorReferenceDTO> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorReferenceDTO> authors) {
        this.authors = authors;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }


}