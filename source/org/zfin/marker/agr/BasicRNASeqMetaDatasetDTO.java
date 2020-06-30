package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import lombok.Getter;
import lombok.Setter;
import java.util.GregorianCalendar;
import java.util.List;

@Getter
@Setter

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicRNASeqMetaDatasetDTO {
    private String title;
    private HtpIDDTO datasetId;
    private GregorianCalendar dateAssigned;
    private List<PublicationAgrDTO> publication;
    private String summary;
    private List<String> categoryTags;


}
