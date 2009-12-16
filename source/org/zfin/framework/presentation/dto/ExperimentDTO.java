package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.Window;
import org.zfin.framework.presentation.gwtutils.StringUtils;

/**
 * Data Transfer Object corresponding to {@link org.zfin.expression.ExpressionExperiment}.
 */
public class ExperimentDTO implements IsSerializable, Comparable<ExperimentDTO> {

    private String experimentZdbID;
    private String geneZdbID;
    private String geneName;
    private String fishName;
    private String fishID;
    private String environment;
    private String environmentID;
    private String assay;
    private String assayAbbreviation;
    private String antibodyID;
    private String antibody;
    private String genbankID;
    private String genbankNumber;
    private String publicationID;
    private String cloneID;
    private String cloneName;
    private String genotypeExperimentID;

    // allows to display an icon if there are expressions associated with the experiment.
    private int numberOfExpressions;

    public String getExperimentZdbID() {
        return experimentZdbID;
    }

    public void setExperimentZdbID(String experimentZdbID) {
        this.experimentZdbID = experimentZdbID;
    }

    public String getGeneZdbID() {
        return geneZdbID;
    }

    public void setGeneZdbID(String geneZdbID) {
        this.geneZdbID = geneZdbID;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getFishName() {
        return fishName;
    }

    public void setFishName(String fishName) {
        this.fishName = fishName;
    }

    public String getEnvironment() {
        return environment;
    }

    /**
     * Remove prefix underscores.
     *
     * @return fransformed value
     */
    public String getEnvironmentDisplayValue() {
        if (StringUtils.isEmpty(environment))
            return "";
        int index = environment.indexOf("_");
        if (index == 0)
            return environment.substring(1);
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
    }

    public String getAntibody() {
        return antibody;
    }

    public void setAntibody(String antibody) {
        this.antibody = antibody;
    }

    public String getGenbankID() {
        return genbankID;
    }

    public void setGenbankID(String genbankID) {
        this.genbankID = genbankID;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public String getFishID() {
        return fishID;
    }

    public void setFishID(String fishID) {
        this.fishID = fishID;
    }

    public String getGenbankNumber() {
        return genbankNumber;
    }

    public void setGenbankNumber(String genbankNumber) {
        this.genbankNumber = genbankNumber;
    }

    public String getAntibodyID() {
        return antibodyID;
    }

    public void setAntibodyID(String antibodyID) {
        this.antibodyID = antibodyID;
    }

    public boolean isUsedInExpressions() {
        return numberOfExpressions > 0;
    }

    public int getNumberOfExpressions() {
        return numberOfExpressions;
    }

    public void setNumberOfExpressions(int numberOfExpressions) {
        this.numberOfExpressions = numberOfExpressions;
    }

    public String getEnvironmentID() {
        return environmentID;
    }

    public void setEnvironmentID(String environmentID) {
        this.environmentID = environmentID;
    }

    public String getCloneID() {
        return cloneID;
    }

    public void setCloneID(String cloneID) {
        this.cloneID = cloneID;
    }

    public String getCloneName() {
        return cloneName;
    }

    public void setCloneName(String cloneName) {
        this.cloneName = cloneName;
    }

    public String getAssayAbbreviation() {
        return assayAbbreviation;
    }

    public void setAssayAbbreviation(String assayAbbreviation) {
        this.assayAbbreviation = assayAbbreviation;
    }

    public String getGenotypeExperimentID() {
        return genotypeExperimentID;
    }

    public void setGenotypeExperimentID(String genotypeExperimentID) {
        this.genotypeExperimentID = genotypeExperimentID;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(getGeneName())) {
            sb.append(getGeneName());
        } else {
            sb.append("----");
        }
        sb.append(" ");
        sb.append(getFishName());
        sb.append("     ");
        sb.append(getEnvironment());
        sb.append("              ");
        sb.append(getAssay());
        if (StringUtils.isNotEmpty(getAntibody())) {
            sb.append("              ");
            sb.append(getAntibody());
        }
        if (StringUtils.isNotEmpty(getGenbankNumber())) {
            sb.append("              ");
            sb.append(getGenbankNumber());
        }
        return sb.toString();

    }

    public int hashCode() {
        //Window.alert("this experiment: "+this);
        int code = 43;
        if (geneName != null)
            code += geneName.hashCode();
        if (fishName != null)
            code += fishName.hashCode();
        if (getEnvironmentDisplayValue() != null)
            code += getEnvironmentDisplayValue().hashCode();
        if (assay != null)
            code += assay.hashCode();
        if (antibody != null)
            code += antibody.hashCode();
        if (genbankNumber != null)
            code += genbankNumber.hashCode();
        return code;
    }

    /**
     * Equality is based on
     * 1) gene
     * 2) fish
     * 3) environment
     * 2) assay
     * 2) antibody
     * 2) genbank ID
     * It does not take into account the experimentID as we may not have an ID yet
     *
     * @param o object
     * @return hashcode
     */
    public boolean equals(Object o) {
        if (!(o instanceof ExperimentDTO))
            return false;

        ExperimentDTO experiment = (ExperimentDTO) o;
        if (!StringUtils.equals(geneName, experiment.geneName))
            return false;
        if (!StringUtils.equals(fishName, experiment.fishName))
            return false;
        if (!StringUtils.equals(getEnvironmentDisplayValue(), experiment.getEnvironmentDisplayValue()))
            return false;
        if (!StringUtils.equals(assay, experiment.assay))
            return false;
        if (!StringUtils.equalsWithNullString(antibody, experiment.antibody))
            return false;
        if (!StringUtils.equalsWithNullString(genbankNumber, experiment.genbankNumber))
            return false;
        return true;
    }

    public int compareTo(ExperimentDTO compExperiment) {
        if (compExperiment == null)
            return -1;
        if (geneName == null && compExperiment.getGeneName() != null)
            return -1;
        if (geneName != null && compExperiment.getGeneName() == null)
            return 1;
        if (geneName != null && compExperiment.getGeneName() != null)
            if (!geneName.equals(compExperiment.getGeneName()))
                return geneName.compareTo(compExperiment.getGeneName());
        if (!fishName.equals(compExperiment.getFishName()))
            return fishName.compareTo(compExperiment.getFishName());
        if (!getEnvironmentDisplayValue().equals(compExperiment.getEnvironment()))
            return getEnvironmentDisplayValue().toLowerCase().compareTo(compExperiment.getEnvironmentDisplayValue().toLowerCase());
        if (!assay.equals(compExperiment.getAssay()))
            return assay.compareToIgnoreCase(compExperiment.getAssay());
        if (antibody == null)
            return -1;
        if (compExperiment.getAntibody() == null)
            return +1;
        if (!antibody.equals(compExperiment.getAntibody()))
            return antibody.compareTo(compExperiment.getAntibody());
        return 0;
    }
}
