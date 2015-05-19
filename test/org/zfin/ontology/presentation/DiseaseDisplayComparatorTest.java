package org.zfin.ontology.presentation;

import org.junit.Test;
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
        aceDiseaseDisplay1.setOmimNumber("104300");
        aceDiseaseDisplay1.setOmimTerm("{Alzheimer disease, susceptibility to}");

        DiseaseDisplay aceDiseaseDisplay2 = new DiseaseDisplay();
        aceDiseaseDisplay2.setOmimNumber("267430");
        aceDiseaseDisplay2.setOmimTerm("Renal tubular dysgenesis");

        // diseaseDisplay with dieaseTerm is list before diseaseDisplay with no dieaseTerm
        assertEquals(-1, aceDiseaseDisplay1.compareTo(aceDiseaseDisplay2));

        DiseaseDisplay aceDiseaseDisplay3 = new DiseaseDisplay();
        aceDiseaseDisplay3.setOmimNumber("612624");
        aceDiseaseDisplay3.setOmimTerm("{Microvascular complications of diabetes 3}");

        // For diseaseDisplay with the same dieaseTerm or same null, and with OMIM phenotype number, those without any brackets [ ], braces { }, or question markers are listed alphabetically first
        assertEquals(-1, aceDiseaseDisplay2.compareTo(aceDiseaseDisplay3));

        DiseaseDisplay aceDiseaseDisplay4 = new DiseaseDisplay();
        aceDiseaseDisplay4.setOmimTerm("[Angiotensin I-converting enzyme, benign serum increase]");

        // For diseaseDisplay with the same dieaseTerm or same null, those OMIM phenotype number is list before those with no OMIM phenotype number
        assertEquals(-1, aceDiseaseDisplay3.compareTo(aceDiseaseDisplay4));
    }


}
