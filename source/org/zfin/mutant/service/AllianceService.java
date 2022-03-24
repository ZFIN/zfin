package org.zfin.mutant.service;

import org.alliancegenome.curation_api.model.entities.ontology.NCBITaxonTerm;

public class AllianceService {

    public static final String ZEBRAFISH_TAXID = "NCBITaxon:7955";

    public static NCBITaxonTerm getNcbiTaxonTerm() {
        NCBITaxonTerm term = new NCBITaxonTerm();
        term.setCurie(ZEBRAFISH_TAXID);
        return term;
    }

}
