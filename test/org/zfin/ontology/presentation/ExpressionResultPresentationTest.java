package org.zfin.ontology.presentation;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.expression.ExpressionResult;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

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
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(superterm);
        entity.setSubterm(subterm);
        result.setEntity(entity);

        String hyperlink = ExpressionResultPresentation.getLink((ExpressionResult) null, true);
        assertNull(hyperlink);

        hyperlink = ExpressionResultPresentation.getLink(result,true);
        assertNotNull(hyperlink);
        assertEquals( "<span title=\"The gene was reported as NOT expressed in this structure.\">(not)</span>&nbsp;<span class=\"post-composed-term-link\"><a href=\"/action/ontology/post-composed-term-detail?superTermID=TERM:eye&subTermID=TERM:melanocyte\"><span class=\"post-composed-term-name\">eye&nbsp;melanocyte</span></a></span>", hyperlink);
    }


    private GenericTerm createTermByName(String name) {
        GenericTerm term = new GenericTerm();
        term.setTermName(name);
        term.setZdbID("ZDB-TERM-" + name);
        term.setOboID("TERM:"+ name);
        term.setOntology(Ontology.ANATOMY);
        return term;
    }

}
