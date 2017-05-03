package org.zfin.marker.agr;

import java.util.HashMap;
import java.util.Map;

public class CrossReferenceDTO {

    private String dataProvider;
    private String id;

    public CrossReferenceDTO(String dataProvider, String id) {
        this.dataProvider = dataProvider;
        this.id = id;
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
        dbNameMap.put("Ensembl", "PMID");
    }


    public String getGlobalID() {
        return dbNameMap.get(dataProvider) + ":" + id;
    }
}
