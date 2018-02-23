package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CrossReferenceDTO {
    @JsonIgnore
    private String dataProvider;
    @JsonIgnore

    @JsonProperty("data")
    private String id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> pages;

    public CrossReferenceDTO(String dataProvider, String localID, List<String> pages) {
        this.dataProvider = dataProvider;
        this.id = dbNameMap.get(dataProvider) + ":" + localID;
        this.pages = pages;
        if (!dbNameMap.keySet().contains(dataProvider))
            throw new RuntimeException("Could not find external DB " + dataProvider);
    }

    public String getId() {
        return id;
    }

    public String getDataProvider() {
        return dataProvider;
    }

    public List<String> getPages() { return pages; }

    static Map<String, String> dbNameMap = new HashMap<>();
    static Map<String, String> pageMap = new HashMap<>();

    static {
        dbNameMap.put("ZFIN", "ZFIN");
        dbNameMap.put("NCBIGene", "NCBI_Gene");
        dbNameMap.put("UniProtKB", "UniProtKB");
        dbNameMap.put("Ensembl", "ENSEMBL");
        dbNameMap.put("PMID", "PMID");
        dbNameMap.put("PANTHER", "PANTHER");
    }


    // public String getGlobalID() {
    //    return dbNameMap.get(dataProvider) + ":" + id;
    //}


}