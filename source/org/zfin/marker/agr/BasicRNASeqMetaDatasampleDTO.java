package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Setter;
import lombok.Getter;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasicRNASeqMetaDatasampleDTO {

    private HtpIDDTO sampleId;
    private String sampleTitle;
    private String abundance;
    private String sampleType;
    private BioSampleAgeDTO sampleAge;
    private List<ExpressionTermIdentifiersDTO> sampleLocation;
    private HtpGenomicInformationDTO genomicInformation;
    private String sex;
    private String assayType;
    private List<String> assemblyVersion;
    private List<String> datasetId;
    @JsonSerialize(using = JsonDateSerializer.class)
    private GregorianCalendar dateAssigned;
    private String sequencingFormat;

}
