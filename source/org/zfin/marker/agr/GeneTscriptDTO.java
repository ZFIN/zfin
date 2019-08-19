package org.zfin.marker.agr;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneTscriptDTO {

    private String symbol;
    private String name;
    private String soTermId;
    private static final String geneLiteratureUrlPrefix = "http://zfin.org/action/marker/citation-list/";
    private String geneLiteratureUrl;

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    private List<String> synonyms;

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    private String primaryId;



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
