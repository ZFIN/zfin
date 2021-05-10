package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptDTO extends ZfinDTO {

    private String symbol;
    private String name;

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    private String soTermId;
    private String sequence;

    public List<String> getSymbolSynonyms() {
        return symbolSynonyms;
    }



    public void setSymbolSynonyms(List<String> symbolSynonyms) {
        this.symbolSynonyms = symbolSynonyms;
    }

    private String url;
    protected List<String> symbolSynonyms;

    public List<String> getCrossReferenceIds() {
        return crossReferenceIds;
    }

    public void setCrossReferenceIds(List<String> crossReferenceIds) {
        this.crossReferenceIds = crossReferenceIds;
    }

    private List<String> crossReferenceIds;
    private GeneTscriptDTO gene;

    public GeneTscriptDTO getGene() {
        return gene;
    }

    public void setGene(GeneTscriptDTO gene) {
        this.gene = gene;
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


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //public String getGeneLiteratureUrl() {
    //    String returnString = geneLiteratureUrlPrefix;
    //    if (primaryId.startsWith(ZFIN))
    //        returnString += primaryId.replace(ZFIN, "");
    //    return returnString;
    //}
}
