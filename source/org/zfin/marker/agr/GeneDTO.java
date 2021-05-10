package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneDTO {

    private String symbol;
    private String name;
    private String soTermId;
    private BasicGeneticEntity basicGeneticEntity;
    private static final String geneLiteratureUrlPrefix = "http://zfin.org/action/marker/citation-list/";
    private String geneLiteratureUrl;


    public BasicGeneticEntity getBasicGeneticEntity() {
        return basicGeneticEntity;
    }

    public void setBasicGeneticEntity(BasicGeneticEntity basicGeneticEntity) {
        this.basicGeneticEntity = basicGeneticEntity;
    }

    public String getGeneLiteratureUrl() {
        return geneLiteratureUrl;
    }

    public void setGeneLiteratureUrl(String geneLiteratureUrl) {
        this.geneLiteratureUrl = geneLiteratureUrl;
    }

    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
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


}
