package org.zfin.ontology.datatransfer;

import org.junit.Test;
import org.zfin.ontology.datatransfer.service.LoadOntology;

import static org.junit.Assert.assertEquals;

/**
 * Load a given ontology.
 */
public class LoadOntologyTest {


    @Test
    public void getRevisionFromGeneOntologyFile(){
        String revision = "cvs version: $Revision: 1.1416 $";
        assertEquals("revision number", "1.1416", LoadOntology.getRevisionFromComment(revision));
    }
}
