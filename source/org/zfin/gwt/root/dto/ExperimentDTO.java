package org.zfin.gwt.root.dto;

import java.util.List;

/**
 * GWT version of Environment corresponding to {@link org.zfin.expression.Experiment}.
 */
public class ExperimentDTO extends RelatedEntityDTO {

    private String zdbID;
    private String name;
    public static final String STANDARD = "Standard";
    public static final String GENERIC_CONTROL = "Generic-control";


    public Boolean getIsUsedInExpression() {
        return isUsedInExpression;
    }

    public void setIsUsedInExpression(Boolean isUsedInExpression) {
        this.isUsedInExpression = isUsedInExpression;
    }

    public Boolean getIsUsedInPhenotype() {
        return isUsedInPhenotype;
    }

    public void setIsUsedInPhenotype(Boolean isUsedInPhenotype) {
        this.isUsedInPhenotype = isUsedInPhenotype;
    }

    public Boolean getIsUsedInDisease() {
        return isUsedInDisease;
    }

    public void setIsUsedInDisease(Boolean isUsedInDisease) {
        this.isUsedInDisease = isUsedInDisease;
    }

    public List<ConditionDTO> conditionDTOList;
    public Boolean isUsedInExpression;
    public Boolean isUsedInPhenotype;
    public Boolean isUsedInDisease;

    public static String getSTANDARD() {
        return STANDARD;
    }





    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public List<ConditionDTO> getConditionDTOList() {
        return conditionDTOList;
    }

    public void setConditionDTOList(List<ConditionDTO> conditionDTOList) {
        this.conditionDTOList = conditionDTOList;
    }

    public void setName(String name) {
        if (name.startsWith("_") && name.substring(1).equals(STANDARD))
            name = STANDARD;
        if (name.startsWith("_") && name.substring(1).equals(GENERIC_CONTROL))
            name = GENERIC_CONTROL;
        this.name = name;
    }

    @Override
    public String toString() {
        return "ExperimentDTO{" +
                "zdbID='" + zdbID + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    /**
     * Order is:
     * 1) Standard
     * 2) Generic-Control
     * 3) alphabetical case insensitive order
     *
     * @param o environment DTO
     * @return integer: -1, 0, 1
     */
    public int compareTo(Object o) {
        if (!(o instanceof ExperimentDTO))
            return 1;
        ExperimentDTO dto = (ExperimentDTO) o;
        if (name.equals(STANDARD))
            return -1;
        String nameToCompare = dto.getName();
        if (name.equals(GENERIC_CONTROL) && !nameToCompare.equals(STANDARD))
            return -1;
        if (name.equals(GENERIC_CONTROL) && nameToCompare.equals(STANDARD))
            return 1;
        if (nameToCompare.equals(STANDARD) || nameToCompare.equals(GENERIC_CONTROL))
            return 1;
        return name.compareTo(dto.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentDTO environmentDTO = (ExperimentDTO) o;

        if (name != null ? !name.equals(environmentDTO.name) : environmentDTO.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public boolean isStandard() {
        return name.equals(STANDARD) || name.equals(GENERIC_CONTROL);
    }
}
