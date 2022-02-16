package org.zfin.mutant.service;

import lombok.extern.log4j.Log4j2;
import org.alliancegenome.curation_api.model.entities.AGMDiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.AffectedGenomicModel;
import org.alliancegenome.curation_api.model.entities.DiseaseAnnotation;
import org.alliancegenome.curation_api.model.entities.Reference;
import org.alliancegenome.curation_api.model.entities.ontology.DOTerm;
import org.alliancegenome.curation_api.model.entities.ontology.EcoTerm;
import org.alliancegenome.curation_api.model.entities.ontology.NCBITaxonTerm;
import org.zfin.alliancegenome.AllianceRestManager;
import org.zfin.alliancegenome.DiseaseAnnotationRESTInterfaceAlliance;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;

import java.util.List;

public class AllianceService {

    public static final String ZEBRAFISH_TAXID = "taxon:7955";

    public static NCBITaxonTerm getNcbiTaxonTerm() {
        NCBITaxonTerm term = new NCBITaxonTerm();
        term.setName(ZEBRAFISH_TAXID);
        return term;
    }

}
