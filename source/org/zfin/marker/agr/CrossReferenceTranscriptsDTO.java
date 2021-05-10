package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrossReferenceTranscriptsDTO {

    private String id;


    @JsonIgnore
    private String dataProvider;

    public CrossReferenceTranscriptsDTO(String dataProvider, String localID) {
        this.dataProvider = dataProvider;
        if (dataProvider == localID) {
            this.id = dbNameMap.get(dataProvider);
        }
        else {
            this.id = dbNameMap.get(dataProvider) + ":" + localID;
        }

        if (!dbNameMap.keySet().contains(dataProvider))
            throw new RuntimeException("Could not find external DB " + dataProvider);

    }

    public String getId() {
        return id;
    }

    public String getDataProvider() {
        return dataProvider;
    }



    static Map<String, String> dbNameMap = new HashMap<>();

    static {
        dbNameMap.put("ZFIN", "ZFIN");
        dbNameMap.put("NCBIGene", "NCBI_Gene");
        dbNameMap.put("UniProtKB", "UniProtKB");
        dbNameMap.put("Ensembl", "ENSEMBL");
        dbNameMap.put("PMID", "PMID");
        dbNameMap.put("PANTHER", "PANTHER");
        dbNameMap.put("miRBASE Mature", "miRBASE");
    }

}
