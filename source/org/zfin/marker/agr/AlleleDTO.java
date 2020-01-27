package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleDTO extends ZfinDTO {

  private String symbol;
  private String gene;
  private List<CrossReferenceDTO> crossReferences;
  private String symbolText;
  private String construct;
  private String alleleDescription;
  private String constructInsertionType;

  public String getConstructInsertionType() {
    return constructInsertionType;
  }

  public void setConstructInsertionType(String constructInsertionType) {
    this.constructInsertionType = constructInsertionType;
  }



  public String getAlleleDescription() {
    return alleleDescription;
  }

  public void setAlleleDescription(String alleleDescription) {
    this.alleleDescription = alleleDescription;
  }



  public String getConstruct() {
    return construct;
  }

  public void setConstruct(String construct) {
    this.construct = construct;
  }

  public List<CrossReferenceDTO> getCrossReferences() {
    return crossReferences;
  }

  public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
    this.crossReferences = crossReferences;
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
  public String getSymbolText() {
    return symbolText;
  }

  public void setSymbolText(String symbolText) {
    this.symbolText = symbolText;
  }


}
