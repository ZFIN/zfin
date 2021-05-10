package org.zfin.ontology.presentation;

import org.junit.Test;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import static org.junit.Assert.assertEquals;

public class DiseaseDisplayComparatorTest {

    //private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    @Test
    public void testDiseaseDisplayComparator() {
        // use the diseases with gene ace ZDB-GENE-030131-1826
        DiseaseDisplay aceDiseaseDisplay1 = new DiseaseDisplay();
        GenericTerm alzheimer = new GenericTerm();
        alzheimer.setZdbID("ZDB-TERM-150506-1627");
        alzheimer.setOntology(Ontology.DISEASE_ONTOLOGY);
        alzheimer.setTermName("Alzheimer's disease");
        aceDiseaseDisplay1.setDiseaseTerm(alzheimer);
        OmimPhenotype omimPhenotype1 = new OmimPhenotype();
        omimPhenotype1.setName("{Alzheimer disease, susceptibility to}");
        omimPhenotype1.setOmimNum("104300");
        aceDiseaseDisplay1.setOmimPhenotype(omimPhenotype1);

        OmimPhenotype omimPhenotype2 = new OmimPhenotype();
        omimPhenotype2.setName("Renal tubular dysgenesis");
        omimPhenotype2.setOmimNum("267430");
        DiseaseDisplay aceDiseaseDisplay2 = new DiseaseDisplay();
        aceDiseaseDisplay2.setOmimPhenotype(omimPhenotype2);

        // diseaseDisplay with dieaseTerm is list before diseaseDisplay with no dieaseTerm
        assertEquals(-1, aceDiseaseDisplay1.compareTo(aceDiseaseDisplay2));

        OmimPhenotype omimPhenotype3 = new OmimPhenotype();
        omimPhenotype3.setName("{Microvascular complications of diabetes 3}");
        omimPhenotype3.setOmimNum("612624");
        DiseaseDisplay aceDiseaseDisplay3 = new DiseaseDisplay();
        aceDiseaseDisplay3.setOmimPhenotype(omimPhenotype3);

        // For diseaseDisplay with the same dieaseTerm or same null, and with OMIM phenotype number, those without any brackets [ ], braces { }, or question markers are listed alphabetically first
        assertEquals(-1, aceDiseaseDisplay2.compareTo(aceDiseaseDisplay3));

        OmimPhenotype omimPhenotype4 = new OmimPhenotype();
        omimPhenotype4.setName("[Angiotensin I-converting enzyme, benign serum increase]");
        DiseaseDisplay aceDiseaseDisplay4 = new DiseaseDisplay();
        aceDiseaseDisplay4.setOmimPhenotype(omimPhenotype4);

        // For diseaseDisplay with the same dieaseTerm or same null, those OMIM phenotype number is list before those with no OMIM phenotype number
        assertEquals(-1, aceDiseaseDisplay3.compareTo(aceDiseaseDisplay4));
    }


}
