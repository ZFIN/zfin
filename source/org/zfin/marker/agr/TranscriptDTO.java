package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptDTO extends ZfinDTO {

    private String symbol;
    private String name;
    private String soTermId;

    private List<CrossReferenceTranscriptsDTO> crossReferences;
    private List<GeneTscriptDTO> genes;



    public List<GeneTscriptDTO> getGenes() {
        return genes;
    }

    public void setGenes(List<GeneTscriptDTO> genes) {
        this.genes = genes;
    }




    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
    }

    public List<CrossReferenceTranscriptsDTO> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<CrossReferenceTranscriptsDTO> crossReferences) {
        this.crossReferences = crossReferences;
    }

    //public String getGeneLiteratureUrl() {
    //    String returnString = geneLiteratureUrlPrefix;
    //    if (primaryId.startsWith(ZFIN))
    //        returnString += primaryId.replace(ZFIN, "");
    //    return returnString;
    //}
}
