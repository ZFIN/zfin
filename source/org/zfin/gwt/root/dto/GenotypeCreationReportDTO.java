package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class GenotypeCreationReportDTO implements IsSerializable {

    private GenotypeDTO genotypeDTO;
    private String reportMessage = "";

    public void setGenotypeDTO(GenotypeDTO genotypeDTO) {
        this.genotypeDTO = genotypeDTO;
    }

    public GenotypeDTO getGenotypeDTO() {
        return genotypeDTO;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public void addMessage(String message) {
        if (reportMessage.length() > 0)
            reportMessage += ", ";
        reportMessage += message;
    }
}
