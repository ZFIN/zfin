package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleDTO extends ZfinDTO {

  private String symbol;
  private String geneId;
  private List<String> synonyms;
  private Set<String> secondaryIds;
  private String gene;
  private CrossReferenceDTO crossReference;

  public CrossReferenceDTO getCrossReference() {
    return crossReference;
  }

  public void setCrossReference(CrossReferenceDTO crossReference) {
    this.crossReference = crossReference;
  }

  public String getGene() {
    return gene;
  }

  public void setGene(String gene) {
    this.gene = gene;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

}
