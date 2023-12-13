package org.zfin.uniprot.dto;

public record UniProtLoadSummaryDTO(String description, int beforeLoadCount, int afterLoadCount) {
    public void debugToStdOut() {
        //convert above to fixed width output for a single row
        System.out.printf("%-40s %10d %10d%n", description, beforeLoadCount, afterLoadCount);
    }
}
