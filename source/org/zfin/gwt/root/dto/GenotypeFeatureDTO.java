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
}

