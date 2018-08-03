package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleDTO extends ZfinDTO {

  private String symbol;
  private String geneId;
  private List<CrossReferenceDTO> crossReferences;
  private String symbolText;

  public List<CrossReferenceDTO> getCrossReferences() {
    return crossReferences;
  }

  public void setCrossReferences(List<CrossReferenceDTO> crossReferences) {
    this.crossReferences = crossReferences;
  }



  public String getGeneId() {
    return geneId;
  }

  public void setGeneId(String geneId) {
    this.geneId = geneId;
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
