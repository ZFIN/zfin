package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllAlleleDTO {

  @JsonProperty("data")
  private List<AlleleDTO> alleles;
  private MetaDataDTO metaData;


}