package org.zfin.uniprot.dto;

import java.util.HashMap;
import java.util.Map;

public record MarkerToProteinDTO(String markerZdbID, String accession) {
    public static MarkerToProteinDTO fromMap(Map<String, String> map) {
        return new MarkerToProteinDTO(map.get("markerZdbID"), map.get("accession"));
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("markerZdbID", markerZdbID);
        map.put("accession", accession);
        return map;
    }
}
