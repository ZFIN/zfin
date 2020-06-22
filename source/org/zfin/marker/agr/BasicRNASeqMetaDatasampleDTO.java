package org.zfin.marker.agr;

import java.util.GregorianCalendar;
import java.util.List;

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

    public List<ExpressionTermIdentifiersDTO> getSampleLocation() {
        return sampleLocation;
    }

    public void setSampleLocation(List<ExpressionTermIdentifiersDTO> sampleLocation) {
        this.sampleLocation = sampleLocation;
    }

    public HtpIDDTO getSampleId() {
        return sampleId;
    }

    public void setSampleId(HtpIDDTO sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleTitle() {
        return sampleTitle;
    }

    public void setSampleTitle(String sampleTitle) {
        this.sampleTitle = sampleTitle;
    }

    public String getAbundance() {
        return abundance;
    }

    public void setAbundance(String abundance) {
        this.abundance = abundance;
    }

    public String getSampleType() {
        return sampleType;
    }

    public void setSampleType(String sampleType) {
        this.sampleType = sampleType;
    }

    public BioSampleAgeDTO getSampleAge() {
        return sampleAge;
    }

    public void setSampleAge(BioSampleAgeDTO sampleAge) {
        this.sampleAge = sampleAge;
    }

    public HtpGenomicInformationDTO getGenomicInformation() {
        return genomicInformation;
    }

    public void setGenomicInformation(HtpGenomicInformationDTO genomicInformation) {
        this.genomicInformation = genomicInformation;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAssayType() {
        return assayType;
    }

    public void setAssayType(String assayType) {
        this.assayType = assayType;
    }

    public String getAssemblyVersion() {
        return assemblyVersion;
    }

    public void setAssemblyVersion(String assemblyVersion) {
        this.assemblyVersion = assemblyVersion;
    }

    public List<String> getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(List<String> datasetId) {
        this.datasetId = datasetId;
    }

    public GregorianCalendar getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(GregorianCalendar dateAssigned) {
        this.dateAssigned = dateAssigned;
    }
    

}
