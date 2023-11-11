package org.zfin.uniprot.interpro;

import java.util.HashMap;
import java.util.Map;

public record EntryListItemDTO(String accession, String type, String name) {
    public static EntryListItemDTO fromMap(Map<String, String> map) {
        return new EntryListItemDTO(map.get("accession"), map.get("type"), map.get("name"));
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("accession", accession);
        map.put("type", type);
        map.put("name", name);
        return map;
    }
}
