package org.zfin.datatransfer.ncbi.dto;

import java.util.List;

public record GeneInfoDTO(String taxID, String geneID, String symbol, String locusTag, String synonyms, String dbXrefs,
    String chromosome, String mapLocation, String description, String typeOfGene, String symbolFromNomenclatureAuthority,
    String fullNameFromNomenclatureAuthority, String nomenclatureStatus, String otherDesignations, String modificationDate,
    String featureType
) implements LoadFileDTOInterface {
    public GeneInfoDTO(String[] fields) {
        this(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7], fields[8], fields[9], fields[10], fields[11], fields[12], fields[13], fields[14], fields[15]);
    }
    public GeneInfoDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Override
    public List<String> expectedHeaders() {
        return List.of(
                "#tax_id",
                "GeneID",
                "Symbol",
                "LocusTag",
                "Synonyms",
                "dbXrefs",
                "chromosome",
                "map_location",
                "description",
                "type_of_gene",
                "Symbol_from_nomenclature_authority",
                "Full_name_from_nomenclature_authority",
                "Nomenclature_status",
                "Other_designations",
                "Modification_date",
                "Feature_type"
        );
    }

    @Override
    public boolean includeThisRecord() {
        return "7955".equals(taxID);
    }
}
