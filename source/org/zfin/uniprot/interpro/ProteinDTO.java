package org.zfin.uniprot.interpro;

import java.util.HashMap;
import java.util.Map;

public record ProteinDTO(String accession, Integer length) {
    public static ProteinDTO fromMap(Map<String, String> map) {
        return new ProteinDTO(map.get("accession"), Integer.parseInt(map.get("length")));
    }
    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("accession", accession);
        map.put("length", length.toString());
        return map;
    }
}
