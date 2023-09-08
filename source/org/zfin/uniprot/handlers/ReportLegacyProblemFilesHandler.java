package org.zfin.uniprot.handlers;

import org.zfin.uniprot.UniProtLoadAction;
import org.zfin.uniprot.UniProtLoadContext;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This handler reports the accessions that we encounter that were previously in the legacy problem files.
 * Eventually, this class should become unnecessary.
 */
public class ReportLegacyProblemFilesHandler implements UniProtLoadHandler {

    @Override
    public void handle(Map<String, RichSequenceAdapter> uniProtRecords, Set<UniProtLoadAction> actions, UniProtLoadContext context) {
        Map<String, String> legacyProblemFiles = getLegacyProblemFiles();
        for (Map.Entry<String, RichSequenceAdapter> entry : uniProtRecords.entrySet()) {
            String accession = entry.getKey();
            if (legacyProblemFiles.containsKey(accession)) {
                UniProtLoadAction action = new UniProtLoadAction();
                action.setAccession(accession);
                action.setType(UniProtLoadAction.Type.INFO);
                action.setSubType(UniProtLoadAction.SubType.LEGACY_PROBLEM_FILE);
                action.setDetails("This accession was in " + legacyProblemFiles.get(accession) + "\n\n" + legacyProblemFiles.get(accession));
            }
        }
    }

    public Map<String, String> getLegacyProblemFiles() {
        Map<String, String> legacyAccessions = new HashMap<>();
        legacyAccessions.put("A0A0G2KQ55", "prob10.txt");
        legacyAccessions.put("A0A0R4IRP9", "prob10.txt");
        legacyAccessions.put("A0A140LGW1", "prob10.txt");
        legacyAccessions.put("A0A1L1QZE3", "prob10.txt");
        legacyAccessions.put("A0A2R8PZR3", "prob10.txt");
        legacyAccessions.put("A0A2R8Q6R8", "prob10.txt");
        legacyAccessions.put("A0A2R8QJM0", "prob10.txt");
        legacyAccessions.put("A0A8M1PSA1", "prob10.txt");
        legacyAccessions.put("A0A8M1PZB9", "prob10.txt");
        legacyAccessions.put("A0A8M1RH71", "prob10.txt");
        legacyAccessions.put("A0A8M1RJ80", "prob10.txt");
        legacyAccessions.put("A0A8M1RSP7", "prob10.txt");
        legacyAccessions.put("A0A8M2BBQ4", "prob10.txt");
        legacyAccessions.put("A0A8M2BBU7", "prob10.txt");
        legacyAccessions.put("A0A8M2BEQ9", "prob10.txt");
        legacyAccessions.put("A0A8M2BHX4", "prob10.txt");
        legacyAccessions.put("A0A8M2BI56", "prob10.txt");
        legacyAccessions.put("A0A8M2BJW9", "prob10.txt");
        legacyAccessions.put("A0A8M2BJY8", "prob10.txt");
        legacyAccessions.put("A0A8M3AGV1", "prob10.txt");
        legacyAccessions.put("A0A8M3AI12", "prob10.txt");
        legacyAccessions.put("A0A8M3AI36", "prob10.txt");
        legacyAccessions.put("A0A8M3AKI0", "prob10.txt");
        legacyAccessions.put("A0A8M3APD0", "prob10.txt");
        legacyAccessions.put("A0A8M3AS57", "prob10.txt");
        legacyAccessions.put("A0A8M3B0G2", "prob10.txt");
        legacyAccessions.put("A0A8M3B5V0", "prob10.txt");
        legacyAccessions.put("A0A8M3B6L0", "prob10.txt");
        legacyAccessions.put("A0A8M3B6X0", "prob10.txt");
        legacyAccessions.put("A0A8M6YW07", "prob10.txt");
        legacyAccessions.put("A0A8M6YWL9", "prob10.txt");
        legacyAccessions.put("A0A8M6YYH3", "prob10.txt");
        legacyAccessions.put("A0A8M6YZ89", "prob10.txt");
        legacyAccessions.put("A0A8M6Z0J6", "prob10.txt");
        legacyAccessions.put("A0A8M6Z499", "prob10.txt");
        legacyAccessions.put("A0A8M6Z4F7", "prob10.txt");
        legacyAccessions.put("A0A8M6Z527", "prob10.txt");
        legacyAccessions.put("A0A8M6Z5Y8", "prob10.txt");
        legacyAccessions.put("A0A8M6Z8T4", "prob10.txt");
        legacyAccessions.put("A0A8M9NYD4", "prob10.txt");
        legacyAccessions.put("A0A8M9P346", "prob10.txt");
        legacyAccessions.put("A0A8M9P4Q0", "prob10.txt");
        legacyAccessions.put("A0A8M9P708", "prob10.txt");
        legacyAccessions.put("A0A8M9PAH7", "prob10.txt");
        legacyAccessions.put("A0A8M9PAJ6", "prob10.txt");
        legacyAccessions.put("A0A8M9PCE0", "prob10.txt");
        legacyAccessions.put("A0A8M9PCL1", "prob10.txt");
        legacyAccessions.put("A0A8M9PEG8", "prob10.txt");
        legacyAccessions.put("A0A8M9PLR6", "prob10.txt");
        legacyAccessions.put("A0A8M9PLZ6", "prob10.txt");
        legacyAccessions.put("A0A8M9PS14", "prob10.txt");
        legacyAccessions.put("A0A8M9PSE0", "prob10.txt");
        legacyAccessions.put("A0A8M9PSE5", "prob10.txt");
        legacyAccessions.put("A0A8M9PSV6", "prob10.txt");
        legacyAccessions.put("A0A8M9PVW9", "prob10.txt");
        legacyAccessions.put("A0A8M9PX99", "prob10.txt");
        legacyAccessions.put("A0A8M9PXM5", "prob10.txt");
        legacyAccessions.put("A0A8M9PYQ4", "prob10.txt");
        legacyAccessions.put("A0A8M9Q0Y0", "prob10.txt");
        legacyAccessions.put("A0A8M9Q3Y6", "prob10.txt");
        legacyAccessions.put("A0A8M9Q3Z4", "prob10.txt");
        legacyAccessions.put("A0A8M9Q501", "prob10.txt");
        legacyAccessions.put("A0A8M9Q6R7", "prob10.txt");
        legacyAccessions.put("A0A8M9Q8A4", "prob10.txt");
        legacyAccessions.put("A0A8M9QDU6", "prob10.txt");
        legacyAccessions.put("A0A8M9QLB5", "prob10.txt");
        legacyAccessions.put("A0A8M9QN02", "prob10.txt");
        legacyAccessions.put("A0A9F4KUS3", "prob10.txt");
        legacyAccessions.put("A3KQ75", "prob10.txt");
        legacyAccessions.put("E7F004", "prob10.txt");
        legacyAccessions.put("E7FG24", "prob10.txt");
        legacyAccessions.put("F1QJR6", "prob10.txt");
        legacyAccessions.put("F6P9Q3", "prob10.txt");
        legacyAccessions.put("R4GEB4", "prob10.txt");
        legacyAccessions.put("A7MCA7", "prob2.txt");
        legacyAccessions.put("A8E5F7", "prob2.txt");
        legacyAccessions.put("A8KC43", "prob2.txt");
        legacyAccessions.put("B3DG37", "prob2.txt");
        legacyAccessions.put("Q2PRG7", "prob2.txt");
        legacyAccessions.put("Q5BJA5", "prob2.txt");
        legacyAccessions.put("Q7SXM6", "prob2.txt");
        legacyAccessions.put("A3KPN2", "prob3.txt");
        legacyAccessions.put("A5WVU6", "prob3.txt");
        legacyAccessions.put("B0R0L1", "prob3.txt");
        legacyAccessions.put("B0S707", "prob3.txt");
        legacyAccessions.put("A0A4P8NJ80", "prob6.txt");
        legacyAccessions.put("A4FVM2", "prob6.txt");
        legacyAccessions.put("B6UM23", "prob6.txt");
        legacyAccessions.put("U3N5X6", "prob6.txt");
        legacyAccessions.put("X1WG64", "prob6.txt");
        legacyAccessions.put("A2CEY2", "prob8.txt");
        legacyAccessions.put("A7MBT5", "prob8.txt");
        legacyAccessions.put("A9JTE3", "prob8.txt");
        legacyAccessions.put("B0R190", "prob8.txt");
        legacyAccessions.put("F1Q7N5", "prob8.txt");
        legacyAccessions.put("Q6DRK9", "prob8.txt");
        legacyAccessions.put("Q6DRM3", "prob8.txt");
        legacyAccessions.put("Q7SXA1", "prob8.txt");
        return legacyAccessions;
    }
}
