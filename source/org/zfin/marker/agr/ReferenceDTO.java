package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.GregorianCalendar;
import java.util.List;

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
    private List<MODReferenceTypeDTO> sMODReferenceTypes;
    private List<ReferenceTagDTO> tags;
    private List<MESHDetailDTO> meshTerms;
    private List<CrossReferenceDTO> crossReferences;
    private String resourceAbbreviation;
    private List<AuthorReferenceDTO> authors;
    private String abstractText;

}