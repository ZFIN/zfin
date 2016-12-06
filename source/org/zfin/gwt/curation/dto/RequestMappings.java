package org.zfin.gwt.curation.dto;

public enum RequestMappings {

    EXPRESSION_GENES("curation/{publicationID}/genes"),
    EXPRESSION_ANTIBODIES("curation/{publicationID}/antibodies"),
    EXPRESSION_GENBANK_ACCESSIONS("curation/{publicationID}/{geneID}/genbank-accessions"),
    ;
    ;


    private String path;

    RequestMappings(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
