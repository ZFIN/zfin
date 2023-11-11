package org.zfin.uniprot.interpro;

import java.util.HashMap;
import java.util.Map;

//select pti_uniprot_id, pti_interpro_id from protein_to_interpro
public record ProteinToInterproDTO(String uniprot, String interpro) {

    public static ProteinToInterproDTO fromMap(Map<String, String> map) {
        return new ProteinToInterproDTO(map.get("uniprot"), map.get("interpro"));
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("uniprot", uniprot);
        map.put("interpro", interpro);
        return map;
    }
}
