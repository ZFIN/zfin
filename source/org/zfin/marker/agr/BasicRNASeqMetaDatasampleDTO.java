package org.zfin.marker.agr;

import lombok.Setter;
import lombok.Getter;

import java.util.GregorianCalendar;
import java.util.List;

@Getter
@Setter
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
    private String assemblyVersion;
    private List<String> datasetId;
    private GregorianCalendar dateAssigned;
    private String sequencingFormat;

}
