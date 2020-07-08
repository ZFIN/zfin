package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlleleDTO extends ZfinDTO {

  private String symbol;
  private List<CrossReferenceDTO> crossReferences;
  private String symbolText;
  private String alleleDescription;
  private List<AlleleRelationDTO> alleleObjectRelations;


}
