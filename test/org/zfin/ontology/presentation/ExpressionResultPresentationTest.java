package org.zfin.ontology.presentation;

import org.junit.Test;
import org.zfin.expression.ExpressionResult;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;

import static junit.framework.Assert.*;

/**
 * Display the full list of post composed terms and their hyperlink.
 */
public class ExpressionResultPresentationTest extends TermPresentation {

    @Test
    public void createExpressionResultHyperlink() {
        ExpressionResult result = new ExpressionResult();
        GenericTerm superterm = createTermByName("eye");
        GenericTerm subterm = createTermByName("melanocyte");
        result.setSuperterm(superterm);
        result.setSubterm(subterm);

        String hyperlink = ExpressionResultPresentation.getLink((ExpressionResult) null);
        assertNull(hyperlink);

        hyperlink = ExpressionResultPresentation.getLink(result);
        assertNotNull(hyperlink);
        assertEquals("<span title=\"The gene was reported as NOT expressed in this structure.\">(not)</span> <span class=\"postcomposedtermlink\"><a href=\"/action/ontology/term-detail?termID=ZDB-TERM-eye\" name=\"eye\" title=\"Anatomy Ontology\">eye</a>&nbsp;<a href=\"/action/ontology/term-detail?termID=ZDB-TERM-melanocyte\" name=\"melanocyte\" title=\"Anatomy Ontology\">melanocyte</a></span>", hyperlink);
    }


    private GenericTerm createTermByName(String name) {
        GenericTerm term = new GenericTerm();
        term.setTermName(name);
        term.setZdbID("ZDB-TERM-" + name);
        term.setOntology(Ontology.ANATOMY);
        return term;
    }

}
