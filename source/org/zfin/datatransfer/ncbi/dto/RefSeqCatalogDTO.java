package org.zfin.datatransfer.ncbi.dto;

import java.util.List;

public record RefSeqCatalogDTO(String taxID, String species, String accessionVersion, String refSeqReleaseDirectory,
    String refSeqStatus, String length
) implements LoadFileDTOInterface {
    public RefSeqCatalogDTO(String[] fields) {
        this(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);
    }
    public RefSeqCatalogDTO() {
        this(null, null, null, null, null, null);
    }

    @Override
    public List<String> expectedHeaders() {
        return List.of(
                "#tax_id",
                "species",
                "accession.version",
                "refseq_release_directory",
                "refseq_status",
                "length"
        );
    }

    @Override
    public boolean includeThisRecord() {
        return "7955".equals(taxID);
    }
}
