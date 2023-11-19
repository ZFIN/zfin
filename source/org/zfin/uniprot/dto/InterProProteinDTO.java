package org.zfin.uniprot.dto;

import org.zfin.sequence.InterProProtein;

import java.util.HashMap;
import java.util.Map;

public record InterProProteinDTO(String accession, String type, String name) {
    public static InterProProteinDTO fromMap(Map<String, String> map) {
        return new InterProProteinDTO(map.get("accession"), map.get("type"), map.get("name"));
    }

    public Map<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("accession", accession);
        map.put("type", type);
        map.put("name", name);
        return map;
    }

    public InterProProtein toInterProProtein() {
        InterProProtein ip = new InterProProtein();
        ip.setIpID(accession);
        ip.setIpType(type);
        ip.setIpName(name);
        return ip;
    }
}
