package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllAlleleDTO {

  @JsonProperty("data")
  private List<AlleleDTO> alleles;
  private MetaDataDTO metaData;

  public List<AlleleDTO> getAlleles() {
    return alleles;
  }

  public void setAlleles(List<AlleleDTO> alleles) {
    this.alleles = alleles;
  }

  public MetaDataDTO getMetaData() {
    return metaData;
  }

  public void setMetaData(MetaDataDTO metaData) {
    this.metaData = metaData;
  }
}
