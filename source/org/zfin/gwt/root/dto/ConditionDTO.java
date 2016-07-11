package org.zfin.gwt.root.dto;

/**
 * GWT version of Environment corresponding to {@link org.zfin.expression.Experiment}.
 */
public class ConditionDTO extends RelatedEntityDTO {

    private String zdbID;
    public TermDTO zecoTerm;
    public TermDTO chebiTerm;
    public TermDTO aoTerm;
    public TermDTO goCCTerm;
    public TermDTO taxonTerm;
    public String environmentZdbID;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public TermDTO getAoTerm() {
        return aoTerm;
    }

    public void setAoTerm(TermDTO aoTerm) {
        this.aoTerm = aoTerm;
    }

    public TermDTO getGoCCTerm() {
        return goCCTerm;
    }

    public void setGoCCTerm(TermDTO goCCTerm) {
        this.goCCTerm = goCCTerm;
    }

    public TermDTO getChebiTerm() {
        return chebiTerm;
    }

    public void setChebiTerm(TermDTO chebiTerm) {
        this.chebiTerm = chebiTerm;
    }

    public TermDTO getTaxonTerm() {
        return taxonTerm;
    }

    public void setTaxonTerm(TermDTO taxonTerm) {
        this.taxonTerm = taxonTerm;
    }

    public TermDTO getZecoTerm() {
        return zecoTerm;
    }

    public void setZecoTerm(TermDTO zecoTerm) {
        this.zecoTerm = zecoTerm;
    }

    public String getEnvironmentZdbID() {
        return environmentZdbID;
    }

    public void setEnvironmentZdbID(String environmentZdbID) {
        this.environmentZdbID = environmentZdbID;
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
        if (!(o instanceof ConditionDTO))
            return 1;
        ConditionDTO dto = (ConditionDTO) o;
        return name.compareTo(dto.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConditionDTO environmentDTO = (ConditionDTO) o;

        if (name != null ? !name.equals(environmentDTO.name) : environmentDTO.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String getName() {
        if (zecoTerm == null)
            return "";
        String name = zecoTerm.getName();
        if (aoTerm != null)
            name += " : " + aoTerm.getName();
        if (goCCTerm != null)
            name += " : " + goCCTerm.getName();
        if (taxonTerm != null)
            name += " : " + taxonTerm.getName();
        if (chebiTerm != null)
            name += " : " + chebiTerm.getName();
        return name;
    }
}
