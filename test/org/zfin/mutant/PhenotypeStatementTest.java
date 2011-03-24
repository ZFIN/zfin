package org.zfin.mutant;

import org.junit.Test;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import static junit.framework.Assert.assertTrue;

public class PhenotypeStatementTest {

    @Test
    public void testEquality() {
        GenericTerm termOne = new GenericTerm();
        termOne.setZdbID("1");
        termOne.setTermName("super Term");

        GenericTerm termComparison = new GenericTerm();
        termComparison.setZdbID("2");
        termComparison.setTermName("sub term");

        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(termOne);

        GenericTerm relatedTerm = new GenericTerm();
        relatedTerm.setZdbID("3");

        PostComposedEntity relatedEntity = new PostComposedEntity();
        relatedEntity.setSuperterm(relatedTerm);

        PhenotypeStatement statement = new PhenotypeStatement();
        statement.setEntity(entity);
        statement.setRelatedEntity(relatedEntity);

        // contains E1a
        assertTrue(statement.contains(termOne));
        // does not contain
        assertTrue(!statement.contains(termComparison));

        entity.setSubterm(termComparison);
        // contains E1a
        assertTrue(statement.contains(termOne));
        // contains in E1b
        assertTrue(statement.contains(termComparison));
        // contained in E2a
        assertTrue(statement.contains(relatedTerm));

        GenericTerm differentTerm = new GenericTerm();
        differentTerm.setZdbID("8");
        // not contained in E1 or E2
        assertTrue(!statement.contains(differentTerm));


    }
}
