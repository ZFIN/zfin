package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;

/**
 * Data Transfer Object corresponding to {@link org.zfin.expression.ExpressionExperiment}.
 */
public class ExperimentDTO implements IsSerializable, Comparable<ExperimentDTO> {

    private String experimentZdbID;
    private MarkerDTO gene;
    private MarkerDTO antibodyMarker;
    private String fishName;
    private String fishID;
    private EnvironmentDTO environment;
    private String assay;
    private String assayAbbreviation;
    private String genbankID;
    private String genbankNumber;
    private String publicationID;
    private String cloneID;
    private String cloneName;
    private String genotypeExperimentID;
    private String featureID;

    // allows to display an icon if there are expressions associated with the experiment.
    private int numberOfExpressions;

    public String getExperimentZdbID() {
        return experimentZdbID;
    }

    public void setExperimentZdbID(String experimentZdbID) {
        this.experimentZdbID = experimentZdbID;
    }

    public MarkerDTO getGene() {
        return gene;
    }

    public void setGene(MarkerDTO gene) {
        this.gene = gene;
    }

    public MarkerDTO getAntibodyMarker() {
        return antibodyMarker;
    }

    public void setAntibodyMarker(MarkerDTO antibodyMarker) {
        this.antibodyMarker = antibodyMarker;
    }

    public String getFishName() {
        return fishName;
    }

    public void setFishName(String fishName) {
        this.fishName = fishName;
    }

    public String getAssay() {
        return assay;
    }

    public void setAssay(String assay) {
        this.assay = assay;
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

    public boolean isUsedInExpressions() {
        return numberOfExpressions > 0;
    }

    public int getNumberOfExpressions() {
        return numberOfExpressions;
    }

    public void setNumberOfExpressions(int numberOfExpressions) {
        this.numberOfExpressions = numberOfExpressions;
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

    public EnvironmentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentDTO environment) {
        this.environment = environment;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (gene != null) {
            sb.append(gene.getAbbreviation());
        } else {
            sb.append("----");
        }
        sb.append(" ");
        sb.append(fishName);
        sb.append("     ");
        sb.append(environment);
        sb.append("              ");
        sb.append(assay);
        if (antibodyMarker != null) {
            sb.append("              ");
            sb.append(antibodyMarker.getAbbreviation());
        }
        if (StringUtils.isNotEmpty(genbankNumber)) {
            sb.append("              ");
            sb.append(genbankNumber);
        }
        return sb.toString();

    }

    public int hashCode() {
        //Window.alert("this experiment: "+this);
        int code = 43;
        if (gene != null)
            code += gene.getAbbreviation().hashCode();
        if (fishName != null)
            code += fishName.hashCode();
        if (environment != null)
            code += environment.hashCode();
        if (assay != null)
            code += assay.hashCode();
        if (antibodyMarker != null)
            code += antibodyMarker.getAbbreviation().hashCode();
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
        if ((gene == null && experiment.getGene() != null) ||
                (gene != null && experiment.getGene() == null))
            return false;
        if ((gene != null && experiment.getGene() != null) &&
                !StringUtils.equals(gene.getAbbreviation(), experiment.getGene().getAbbreviation()))
            return false;
        if (!StringUtils.equals(fishName, experiment.fishName))
            return false;
        //Window.alert(environment.toString());
        if (!environment.equals(experiment.getEnvironment()))
            return false;
        if (!StringUtils.equals(assay, experiment.assay))
            return false;
        if ((antibodyMarker == null && experiment.getAntibodyMarker() != null) ||
                (antibodyMarker != null && experiment.getAntibodyMarker() == null))
            return false;
        if ((antibodyMarker != null && experiment.getAntibodyMarker() != null) &&
                !StringUtils.equalsWithNullString(antibodyMarker.getAbbreviation(), experiment.getAntibodyMarker().getAbbreviation()))
            return false;
        if (!StringUtils.equalsWithNullString(genbankNumber, experiment.genbankNumber))
            return false;
        return true;
    }

    public int compareTo(ExperimentDTO compExperiment) {
        if (compExperiment == null)
            return -1;
        if (gene == null && compExperiment.getGene() != null)
            return -1;
        if (gene != null && compExperiment.getGene() == null)
            return 1;
        if (gene != null && compExperiment.getGene() != null)
            if (!gene.getAbbreviation().equals(compExperiment.getGene().getAbbreviation()))
                return gene.getAbbreviation().compareTo(compExperiment.getGene().getAbbreviation());
        if (!fishName.equals(compExperiment.getFishName()))
            return fishName.compareTo(compExperiment.getFishName());
        if (!environment.equals(compExperiment.getEnvironment()))
            return environment.compareTo(compExperiment.getEnvironment());
        if (!assay.equals(compExperiment.getAssay()))
            return assay.compareToIgnoreCase(compExperiment.getAssay());
        if (antibodyMarker == null)
            return -1;
        if (compExperiment.getAntibodyMarker() == null)
            return 1;
        if (!antibodyMarker.equals(compExperiment.getAntibodyMarker()))
            return antibodyMarker.getAbbreviation().compareTo(compExperiment.getAntibodyMarker().getAbbreviation());
        return 0;
    }

    public String getFeatureID() {
        return featureID;
    }

    public void setFeatureID(String featureID) {
        this.featureID = featureID;
    }
}
