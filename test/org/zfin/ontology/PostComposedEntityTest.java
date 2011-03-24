package org.zfin.ontology;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class PostComposedEntityTest {

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
        assertTrue(entity.contains(termOne));
        assertTrue(!entity.contains(termComparison));

        entity.setSubterm(termComparison);
        assertTrue(entity.contains(termOne));
        assertTrue(entity.contains(termComparison));
    }
}
