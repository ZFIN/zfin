package org.zfin.orthology;

import java.util.HashMap;
import java.util.Map;

/**
 * mapping of ECO ID to GO evidence Code
 * http://wiki.geneontology.org/index.php/Evidence_Code_Ontology_(ECO)
 */
public class EcoOrthoEvidenceCodeMapping {

    private static Map<String, String> mapping = new HashMap<>(30);

    static {
        mapping.put("CL","ECO:0000177");
        mapping.put("PT","ECO:0000080");
        mapping.put( "AA","ECO:0000031");
        mapping.put( "NT","ECO:0000032");
        mapping.put( "CE","ECO:0000075");
        mapping.put( "FC","ECO:0000088");
        mapping.put( "OT","ECO:0000204");

    }

    public static String getOrthoEvidenceCode(String ecoId) {
        if (!mapping.containsKey(ecoId))
            throw new RuntimeException("Could not find Ortho evidence code for ECO ID: " + ecoId);
        return mapping.get(ecoId);
    }
}
