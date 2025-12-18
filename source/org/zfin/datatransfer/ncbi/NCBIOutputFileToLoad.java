package org.zfin.datatransfer.ncbi;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.zfin.datatransfer.ncbi.NCBIDirectPort.FDCONT_NCBI_GENE_ID;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.PUB_MAPPED_BASED_ON_RNA;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT;
import static org.zfin.datatransfer.ncbi.NCBIDirectPort.PUB_MAPPED_BASED_ON_VEGA;

@Log4j2
public class NCBIOutputFileToLoad {

    // Based on our historical data load process (toLoad.unl which is loaded into temp ncbi_gene table by loadNCBIGeneAccs.sql)
    public record LoadFileRow(String geneID, String accession, Integer length, String fdb, String pub){}

    public List<LoadFileRow> getRows() {
        return toLoadNcbiGenes.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public String getOutput() {
        StringBuilder sb = new StringBuilder();
        String separator = "|";
        for (String geneID : toLoadNcbiGenes.keySet().stream().sorted().toList()) {
            for (LoadFileRow row : toLoadNcbiGenes.get(geneID).stream().sorted((r1, r2) -> r1.accession.compareTo(r2.accession)).toList()) {
                String lengthStr = row.length == null ? "" : row.length.toString();
                sb.append(row.geneID).append(separator)
                        .append(row.accession).append(separator)
                        .append(separator) //placeholder for ZDB ID that is only used after the data is loaded into the DB
                        .append(lengthStr).append(separator)
                        .append(row.fdb).append(separator)
                        .append(row.pub).append("\n");
            }
        }
        return sb.toString();
    }

    //This is the substance of the toLoad.unl file. Organized as a map for easy access by ZFIN Gene ID.
    //Key: ZFIN Gene ID, Value: list of rows to load for that gene
    private Map<String, List<LoadFileRow>> toLoadNcbiGenes = new HashMap<>();

    public void addRow(LoadFileRow row){
        //we only support one-to-one mapping of ZFIN Gene ID to NCBI Gene ID
        if (row.fdb.equals(FDCONT_NCBI_GENE_ID) && hasGeneID(row.geneID, FDCONT_NCBI_GENE_ID)) {
            LoadFileRow existingRow = getFirstByGeneID(row.geneID, FDCONT_NCBI_GENE_ID);
            if (hasHigherPriorityPub(row, existingRow)) {
                log.warn("Replacing existing NCBI Gene ID " + existingRow.accession + " for gene " + row.geneID +
                        " with new NCBI Gene ID " + row.accession + " based on higher priority publication " + row.pub);
                replaceNCBIGeneID(row);
            }
            return;
        }

        getByGeneID(row.geneID).add(row);
    }

    /**
     * Replace existing NCBI Gene ID row for the given gene with the provided row.
     * Logic elsewhere prevents multiple NCBI Gene ID rows for the same gene.
     * @param row
     */
    private void replaceNCBIGeneID(LoadFileRow row) {
        List<LoadFileRow> existingRows = getByGeneID(row.geneID);
        List<LoadFileRow> filteredRows = existingRows.stream()
                .filter(r -> !r.fdb.equals(FDCONT_NCBI_GENE_ID))
                .collect(Collectors.toList());
        filteredRows.add(row);
        toLoadNcbiGenes.put(row.geneID, filteredRows);
    }

    private boolean hasHigherPriorityPub(LoadFileRow row1, LoadFileRow row2) {
        // Priority comparison
        // PUB_MAPPED_BASED_ON_RNA > PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT > PUB_MAPPED_BASED_ON_VEGA
        return switch (row1.pub) {
            case PUB_MAPPED_BASED_ON_RNA -> true;
            case PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT -> row2.pub.equals(PUB_MAPPED_BASED_ON_VEGA);
            case PUB_MAPPED_BASED_ON_VEGA -> false;
            default -> {
                log.warn("Unknown publication type: " + row1.pub);
                yield false;
            }
        };
    }

    private LoadFileRow getFirstByGeneID(String geneID, String fdb) {
        List<LoadFileRow> rows = getByGeneID(geneID, fdb);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    private List<LoadFileRow> getByGeneID(String geneID) {
        return toLoadNcbiGenes.computeIfAbsent(geneID, k -> new ArrayList<>());
    }

    private List<LoadFileRow> getByGeneID(String geneID, String fdb) {
        return getByGeneID(geneID).stream().filter(r -> r.fdb.equals(fdb)).collect(Collectors.toList());
    }

    private boolean hasGeneID(String geneID, String fdb) {
        return getByGeneID(geneID, fdb).size() > 0;
    }

}
