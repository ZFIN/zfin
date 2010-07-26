package org.zfin.ontology;

import com.google.gwt.benchmarks.client.Setup;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class RelationshipDisplayNamesTest {

    @Test
    public void getDisplayNames() {
        String relationshipType = "is_a";
        Ontology ontology = Ontology.ANATOMY;
        String displayName = RelationshipDisplayNames.getRelationshipName(relationshipType, ontology, true);
        assertNotNull(displayName);
        assertEquals("is a type of", displayName);

        displayName = RelationshipDisplayNames.getRelationshipName(relationshipType, ontology, false);
        assertNotNull(displayName);
        assertEquals("has subtype", displayName);

        displayName = RelationshipDisplayNames.getRelationshipName("regulates", ontology, true);
        assertNotNull(displayName);
        assertEquals("regulates", displayName);

        displayName = RelationshipDisplayNames.getRelationshipName("regulates", ontology, false);
        assertNotNull(displayName);
        assertEquals("regulated by", displayName);

        displayName = RelationshipDisplayNames.getRelationshipName("evolves", ontology, true);
        assertNotNull(displayName);
        assertEquals("evolves", displayName);

        displayName = RelationshipDisplayNames.getRelationshipName("evolves", ontology, false);
        assertNotNull(displayName);
        assertEquals("inverse evolves", displayName);
    }

    @Before
    public void setup() {
        // initialize
        new RelationshipDisplayNames();
    }
}