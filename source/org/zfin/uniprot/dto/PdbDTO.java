package org.zfin.uniprot.dto;

import java.util.HashMap;
import java.util.Map;

public record PdbDTO(String uniprot, String pdb) {

    public static PdbDTO fromMap(Map<String, String> map) {
        return new PdbDTO(map.get("uniprot"), map.get("pdb"));
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("uniprot", uniprot);
        map.put("pdb", pdb);
        return map;
    }
}
