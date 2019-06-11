package org.zfin.zebrashare.presentation;

import org.zfin.zebrashare.FeatureCommunityContribution;

public class LineEditBean {

    private FeatureCommunityContribution.FunctionalConsequence functionalConsequence;
    private Boolean adultViable;
    private Boolean maternalZygosityExamined;
    private Boolean currentlyAvailable;
    private String otherLineInformation;

    public FeatureCommunityContribution.FunctionalConsequence getFunctionalConsequence() {
        return functionalConsequence;
    }

    public void setFunctionalConsequence(FeatureCommunityContribution.FunctionalConsequence functionalConsequence) {
        this.functionalConsequence = functionalConsequence;
    }

    public Boolean getAdultViable() {
        return adultViable;
    }

    public void setAdultViable(Boolean adultViable) {
        this.adultViable = adultViable;
    }

    public Boolean getMaternalZygosityExamined() {
        return maternalZygosityExamined;
    }

    public void setMaternalZygosityExamined(Boolean maternalZygosityExamined) {
        this.maternalZygosityExamined = maternalZygosityExamined;
    }

    public Boolean getCurrentlyAvailable() {
        return currentlyAvailable;
    }

    public void setCurrentlyAvailable(Boolean currentlyAvailable) {
        this.currentlyAvailable = currentlyAvailable;
    }

    public String getOtherLineInformation() {
        return otherLineInformation;
    }

    public void setOtherLineInformation(String otherLineInformation) {
        this.otherLineInformation = otherLineInformation;
    }
}
