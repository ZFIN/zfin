package org.zfin.gwt.root.dto;

/**
 */
public class GenotypeFeatureDTO extends RelatedEntityDTO {

    private FeatureDTO featureDTO;
    private ZygosityDTO zygosity;
    private ZygosityDTO maternalZygosity;
    private ZygosityDTO paternalZygosity;

    public FeatureDTO getFeatureDTO() {
        return featureDTO;
    }

    public void setFeatureDTO(FeatureDTO featureDTO) {
        this.featureDTO = featureDTO;
    }

    public ZygosityDTO getMaternalZygosity() {
        return maternalZygosity;
    }

    public void setMaternalZygosity(ZygosityDTO maternalZygosity) {
        this.maternalZygosity = maternalZygosity;
    }

    public ZygosityDTO getPaternalZygosity() {
        return paternalZygosity;
    }

    public void setPaternalZygosity(ZygosityDTO paternalZygosity) {
        this.paternalZygosity = paternalZygosity;
    }

    public ZygosityDTO getZygosity() {
        return zygosity;
    }

    public void setZygosity(ZygosityDTO zygosity) {
        this.zygosity = zygosity;
    }

    public String getZygosityInfo() {
        StringBuilder builder = new StringBuilder("[");
        builder.append(ZygosityDTO.Type.getZygosity(zygosity.getName()).getSymbol());
        builder.append(",");
        builder.append(ZygosityDTO.Type.getZygosity(maternalZygosity.getName()).getSymbol());
        builder.append(",");
        builder.append(ZygosityDTO.Type.getZygosity(paternalZygosity.getName()).getSymbol());
        builder.append("]");
        return builder.toString();

    }

    @Override
    public String toString() {
        String display = featureDTO.getAbbreviation();
        return display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GenotypeFeatureDTO that = (GenotypeFeatureDTO) o;

        if (!featureDTO.equals(that.featureDTO)) return false;
        if (!zygosity.equals(that.zygosity)) return false;
        if (!maternalZygosity.equals(that.maternalZygosity)) return false;
        return paternalZygosity.equals(that.paternalZygosity);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + featureDTO.hashCode();
        result = 31 * result + zygosity.hashCode();
        result = 31 * result + maternalZygosity.hashCode();
        result = 31 * result + paternalZygosity.hashCode();
        return result;
    }
}

