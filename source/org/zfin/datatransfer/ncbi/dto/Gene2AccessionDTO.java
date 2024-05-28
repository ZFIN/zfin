package org.zfin.datatransfer.ncbi.dto;

import java.util.List;

public record Gene2AccessionDTO(String taxID, String geneID, String status, String rnaNucleotideAccessionVersion, String rnaNucleotideGI,
    String proteinAccessionVersion, String proteinGI, String genomicNucleotideAccessionVersion, String genomicNucleotideGI, String startPositionOnTheGenomicAccession,
    String endPositionOnTheGenomicAccession, String orientation, String assembly, String maturePeptideAccessionVersion, String maturePeptideGI, String symbol
) implements LoadFileDTOInterface {
    public Gene2AccessionDTO(String[] fields) {
        this(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8], fields[9], fields[10], fields[11], fields[12], fields[13], fields[14], fields[15]);
    }
    public Gene2AccessionDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Override
    public List<String> expectedHeaders() {
        return List.of(
                "#tax_id",
                "GeneID",
                "status",
                "RNA_nucleotide_accession.version",
                "RNA_nucleotide_gi",
                "protein_accession.version",
                "protein_gi",
                "genomic_nucleotide_accession.version",
                "genomic_nucleotide_gi",
                "start_position_on_the_genomic_accession",
                "end_position_on_the_genomic_accession",
                "orientation",
                "assembly",
                "mature_peptide_accession.version",
                "mature_peptide_gi",
                "Symbol"
        );
    }

    @Override
    public boolean includeThisRecord() {
        return "7955".equals(taxID);
    }
}
