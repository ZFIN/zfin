package org.zfin.datatransfer.go;

import java.util.HashMap;
import java.util.Map;

/**
 * mapping of ECO ID to GO evidence Code
 * http://wiki.geneontology.org/index.php/Evidence_Code_Ontology_(ECO)
 */
public class EcoGoEvidenceCodeMapping {

    private static Map<String, String> mapping = new HashMap<>(30);

    static {
        mapping.put("ECO:0000203", "IEA");
        mapping.put("ECO:0000322", "IEA");
        mapping.put("ECO:0000265", "IEA");
        mapping.put("ECO:0000256", "IEA");
        mapping.put("ECO:0000501", "IEA");
        mapping.put("ECO:0000307", "ND");
        mapping.put("ECO:0000269", "EXP");
        mapping.put("ECO:0000314", "IDA");
        mapping.put("ECO:0000315", "IMP");
        mapping.put("ECO:0000316", "IGI");
        mapping.put("ECO:0000270", "IEP");
        mapping.put("ECO:0000021", "IPI");
        mapping.put("ECO:0000353", "IPI");
        mapping.put("ECO:0000304", "TAS");
        mapping.put("ECO:0000303", "NAS");
        mapping.put("ECO:0000305", "IC");
        mapping.put("ECO:0000306", "IC");
        mapping.put("ECO:0000031", "ISS");
        mapping.put("ECO:0000255", "ISS");
        mapping.put("ECO:0000250", "ISS");
        mapping.put("ECO:0000266", "ISO");
        mapping.put("ECO:0000247", "ISA");
        mapping.put("ECO:0000255", "ISM");
        mapping.put("ECO:0000084", "IGC");
        mapping.put("ECO:0000317", "IGC");
        mapping.put("ECO:0000318", "IBA");
        mapping.put("ECO:0000319", "IBD");
        mapping.put("ECO:0000320", "IKR");
        mapping.put("ECO:0000321", "IRD");
        mapping.put("ECO:0000245", "RCA");
        mapping.put("ECO:0000353", "IPI");
        // duplicate EDO assignment...
//        mapping.put("ECO:0000320", "IMR");
    }

    public static String getGoEvidenceCode(String ecoId) {
        if (!mapping.containsKey(ecoId))
            throw new RuntimeException("Could not find GO evidence code for ECO ID: " + ecoId);
        return mapping.get(ecoId);
    }
}
