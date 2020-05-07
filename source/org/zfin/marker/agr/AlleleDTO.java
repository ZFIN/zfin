package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleDTO extends ZfinDTO {

  private String symbol;
  private List<CrossReferenceDTO> crossReferences;
  private String symbolText;
  private String alleleDescription;
  private List<AlleleRelationDTO> alleleObjectRelations;

  public List<AlleleRelationDTO> getAlleleObjectRelations() {
    return alleleObjectRelations;
  }

  public void setAlleleObjectRelations(List<AlleleRelationDTO> alleleObjectRelations) {
    this.alleleObjectRelations = alleleObjectRelations;
  }



  public String getAlleleDescription() {
    return alleleDescription;
  }

  public void setAlleleDescription(String alleleDescription) {
    this.alleleDescription = alleleDescription;
  }


  public List<CrossReferenceDTO> getCrossReferences() {
    return crossReferences;
  }

  public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
    this.crossReferences = crossReferences;
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
