package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.GregorianCalendar;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.zfin.util.JsonDateSerializer;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ReferenceDTO {

    private String primaryId;
    private String title;
    private String datePublished;
    private String citation;
    private String allianceCategory;
    private String dateArrivedInPubMed;
    private String dateLastModified;
    private String volume;
    private String pages;
    private List<String> keywords;
    private List<String> pubMedType;
    private String publisher;
    @JsonProperty("MODReferenceTypes")
    private List<MODReferenceTypeDTO> MODReferenceTypes;
    private List<ReferenceTagDTO> tags;
    @JsonIgnore
    private List<MESHDetailDTO> meshTerms;
    private List<CrossReferenceDTO> crossReferences;
    private String resourceAbbreviation;
    private List<AuthorReferenceDTO> authors;
    @JsonProperty("abstract")
    private String abstractText;

}