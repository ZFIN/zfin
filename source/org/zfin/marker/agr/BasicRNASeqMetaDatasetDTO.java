package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import lombok.Getter;
import lombok.Setter;
import java.util.GregorianCalendar;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicRNASeqMetaDatasetDTO {
    private String title;
    private HtpIDDTO datasetId;

    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned;
    private List<PublicationAgrDTO> publications;
    private String summary;
    private List<String> categoryTags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HtpIDDTO getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(HtpIDDTO datasetId) {
        this.datasetId = datasetId;
    }

    public GregorianCalendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(GregorianCalendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public List<PublicationAgrDTO> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationAgrDTO> publications) {
        this.publications = publications;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getCategoryTags() {
        return categoryTags;
    }

    public void setCategoryTags(List<String> categoryTags) {
        this.categoryTags = categoryTags;
    }



}
