package org.zfin.datatransfer.ncbi.dto;

import java.util.List;

public record Gene2VegaDTO(String taxID, String geneID, String vegaGeneIdentifier, String rnaNucleotideAccessionVersion,
    String vegaRnaIdentifier, String proteinAccessionVersion, String vegaProteinIdentifier
) implements LoadFileDTOInterface {
    public Gene2VegaDTO(String[] fields) {
        this(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
    }
    public Gene2VegaDTO() {
        this(null, null, null, null, null, null, null);
    }

    @Override
    public List<String> expectedHeaders() {
        return List.of(
                "#tax_id",
                "GeneID",
                "Vega_gene_identifier",
                "RNA_nucleotide_accession.version",
                "Vega_rna_identifier",
                "protein_accession.version",
                "Vega_protein_identifier"
        );
    }

    @Override
    public boolean includeThisRecord() {
        return "7955".equals(taxID);
    }
}
