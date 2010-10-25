package org.zfin.ontology.datatransfer;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

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
