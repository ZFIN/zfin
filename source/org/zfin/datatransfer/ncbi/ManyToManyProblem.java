package org.zfin.datatransfer.ncbi;

import com.mchange.v2.collection.MapEntry;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.zfin.construct.presentation.MarkerNameAndZdbId;
import org.zfin.util.ZfinStringUtils;


import java.util.*;

@Data
public class ManyToManyProblem {

    @Data
    public static class AccessionMapping {
        private String accession; // GenBank or RefSeq accession
        private Set<NameIdPair> ncbiIds = new HashSet<>(); // NCBI IDs associated with this accession
        private Set<NameIdPair> zdbIds = new HashSet<>();  // ZDB IDs associated with this accession

        public AccessionMapping(String accession) {
            this.accession = accession;
        }

        public List<NameIdPair> getZdbIdsForSymbol(String geneSymbol) {
            List<NameIdPair> supportingIDs = new ArrayList<>();
            for(NameIdPair zdbId : zdbIds) {
                if (zdbId.name().equals(geneSymbol)) {
                    supportingIDs.add(zdbId);
                }
            }
            return supportingIDs;
        }

        public List<NameIdPair> getNcbiIdsForSymbol(String geneSymbol) {
            List<NameIdPair> supportingIDs = new ArrayList<>();
            for(NameIdPair ncbiId : ncbiIds) {
                if (ncbiId.name().equals(geneSymbol)) {
                    supportingIDs.add(ncbiId);
                }
            }
            return supportingIDs;
        }

        public String getNcbiIDsString() {
            Set<String> ncbiIDs = new HashSet<>();
            for(NameIdPair ncbiId : this.getNcbiIds()) {
                ncbiIDs.add(ncbiId.id() + " (" + ncbiId.name() + ")");
            }
            return String.join(", ", ncbiIDs);
        }

        public String getZdbIDsString() {
            Set<String> zdbIDs = new HashSet<>();
            for(NameIdPair zdbId : this.getZdbIds()) {
                zdbIDs.add(zdbId.id() + " (" + zdbId.name() + ")");
            }
            return String.join(", ", zdbIDs);
        }
    }

    // List of accession mappings (GenBank/RefSeq)
    private Map<String, AccessionMapping> accessions = new TreeMap<>();


    /**
     * Return a list of issues found in the mappings.
     * Each issue is a Map Entry with the accession as the key and a list of NameIdPairs as the value.
     * The list of NameIdPairs contains the conflicting gene symbols and their supporting IDs.
     * Some are ZDB IDs and some are NCBI IDs.
     *
     * @return the list of issues found
     */
    public Map<String, List<NameIdPair>> getIssues() {
        Map<String, List<NameIdPair>> issues = new TreeMap<>();
        Set<String> distinctGeneSymbolNames = new HashSet<>();
        for(AccessionMapping mapping : accessions.values()) {
            Set<String> ncbiGeneSymbols = new HashSet<>();
            ncbiGeneSymbols.addAll(mapping.getNcbiIds().stream().map(nameIdPair -> nameIdPair.name()).toList());

            Set<String> zdbGeneSymbols = new HashSet<>();
            zdbGeneSymbols.addAll(mapping.getZdbIds().stream().map(nameIdPair -> nameIdPair.name()).toList());

            Set<String> combinedGeneSymbolNames = new HashSet<>();
            combinedGeneSymbolNames.addAll(ncbiGeneSymbols);
            combinedGeneSymbolNames.addAll(zdbGeneSymbols);

            distinctGeneSymbolNames.addAll(combinedGeneSymbolNames);

            if (combinedGeneSymbolNames.size() > 1) {
                String issueMessage = "Conflicting gene symbols for accession " + mapping.getAccession() + ": " + String.join(", ", combinedGeneSymbolNames);
                for(String geneSymbol : combinedGeneSymbolNames) {
                    List<NameIdPair> supportingIDs = mapping.getZdbIdsForSymbol(geneSymbol);
                    if (!supportingIDs.isEmpty()) {
                        issueMessage += "\n\t" + supportingIDs.stream().map(s -> s.id() + " (" + s.name() + ")").toList();
                        issues.computeIfAbsent(mapping.getAccession(), k -> new ArrayList<>()).addAll(supportingIDs);
                    }
                }
                for(String geneSymbol : combinedGeneSymbolNames) {
                    List<NameIdPair> supportingIDs = mapping.getNcbiIdsForSymbol(geneSymbol);
                    if (!supportingIDs.isEmpty()) {
                        issueMessage += "\n\t" + supportingIDs.stream().map(s -> s.id() + " (" + s.name() + ")").toList();
                        issues.computeIfAbsent(mapping.getAccession(), k -> new ArrayList<>()).addAll(supportingIDs);
                    }
                }
            }
        }

        return issues;
    }

    /**
     * Return all the supported ZDB to NCBI ID pairs that do not have conflicting gene symbols.
     * These will each have a 1 to 1 mapping with no conflicts.
     * @return
     */
    public Map<String, Pair<MarkerNameAndZdbId, String>> getSupportedPairs() {
        Map<String, Pair<MarkerNameAndZdbId, String>> supportedPairs = new TreeMap<>();
        for(AccessionMapping mapping : accessions.values()) {
            Set<NameIdPair> ncbiIDs = mapping.getNcbiIds();
            Set<NameIdPair> zdbIDs = mapping.getZdbIds();
            if (ncbiIDs.size() == 1 && zdbIDs.size() == 1) {
                NameIdPair ncbiID = ncbiIDs.iterator().next();
                NameIdPair zdbID = zdbIDs.iterator().next();
                if (ncbiID.name().equals(zdbID.name())) {
                    MarkerNameAndZdbId zdbGene = new MarkerNameAndZdbId();
                    zdbGene.setZdbID(zdbID.id());
                    zdbGene.setLabel(zdbID.name());
                    supportedPairs.put(mapping.getAccession(), Pair.of(zdbGene, ncbiID.id()));
                }
            }
        }

        return supportedPairs;
    }

    public String summary() {
        Map<String, List<NameIdPair>> issues = getIssues();
        Set<String> visitedAccessions = new HashSet<>();
        StringBuilder summary = new StringBuilder();
        summary.append("Total accessions: ").append(accessions.size()).append("\n");

        summary.append("\nAccessions with issues: ").append(issues.size()).append("\n");
        for(String accession : issues.keySet()) {
            List<NameIdPair> conflictingIDs = issues.get(accession);
            summary.append("\t" + accession + " -> " +
                    conflictingIDs.stream().map(id -> id.id() + " (" + id.name() + ")").toList() + "\n");
            visitedAccessions.add(accession);
        }

        summary.append("\nSupported pairs (1 to 1 mapping): ").append(getSupportedPairs().size()).append("\n");
        for(String accession : getSupportedPairs().keySet()) {
            Pair<MarkerNameAndZdbId, String> supportedPair = getSupportedPairs().get(accession);
            summary.append("\t" + accession + " -> " +
                    supportedPair.getLeft().getZdbID() + " (" + supportedPair.getLeft().getLabel() + ") -> " + supportedPair.getRight());
            summary.append("\n");
            visitedAccessions.add(accession);
        }
//        summary.append("Accessions without issues: ").append(accessions.size() - issues.size()).append("\n");

        Set<String> remainingAccessions = new TreeSet<>();
        for(AccessionMapping mapping : accessions.values()) {
            if (!visitedAccessions.contains(mapping.getAccession())) {
                remainingAccessions.add(mapping.getAccession());
            }
        }

        summary.append("\n");
        summary.append("Other accessions: ").append(remainingAccessions.size()).append("\n");
        for(AccessionMapping mapping : accessions.values()) {
            if (remainingAccessions.contains(mapping.getAccession())) {
                summary.append("\t" + mapping.getAccession() + " -> ");
                if (!mapping.getZdbIds().isEmpty()) {
                    summary.append("" + mapping.getZdbIDsString() + "\n");
                }
                if (!mapping.getNcbiIds().isEmpty()) {
                    summary.append("" + mapping.getNcbiIDsString() + "\n");
                }
            }
        }
        return summary.toString();
    }

    public String toJson() {
        return ZfinStringUtils.objectToPrettyJson(this);
    }

    public void addAssociatedDataByZdbID(String zdbIdNtoN, String geneSymbol, Collection<String> refArrayAccsZFIN) {
        NameIdPair markerNameAndZdbId = new NameIdPair(geneSymbol, zdbIdNtoN);
        for (String refAcc : refArrayAccsZFIN) {
            addZDBMapping(refAcc, markerNameAndZdbId);
        }
    }

    public void addAssociatedDataByNcbiGeneID(String ncbiId, String geneSymbol, Collection<String> refArrayAccsNCBI) {
        NameIdPair geneSymbolAndNcbiID = new NameIdPair(geneSymbol, ncbiId);
        for (String refAcc : refArrayAccsNCBI) {
            addNCBIMapping(refAcc, geneSymbolAndNcbiID);
        }
    }

    private void addMapping(String accession, NameIdPair ncbiId, NameIdPair zdbId) {
        AccessionMapping mapping = accessions.get(accession);
        if (mapping == null) {
            mapping = new AccessionMapping(accession);
            accessions.put(accession, mapping);
        }
        if (ncbiId != null) mapping.getNcbiIds().add(ncbiId);
        if (zdbId != null) mapping.getZdbIds().add(zdbId);
    }

    private void addNCBIMapping(String accession, NameIdPair ncbiId) {
        addMapping(accession, ncbiId, null);
    }

    private void addZDBMapping(String accession, NameIdPair zdbId) {
        addMapping(accession, null, zdbId);
    }

}