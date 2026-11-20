package org.zfin.zirc.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.Test;
import org.zfin.zirc.api.ZircMutationFormSchema;
import org.zfin.zirc.api.ZircPhenotypeFormSchema;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * ZFIN-10617: the yes / no / unknown answer, from PATCH value through the
 * entity's two columns and back out to JSON.
 */
public class YesNoUnknownTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    @Test
    public void parsesEveryAcceptedValue() {
        assertNull(YesNoUnknown.fromJson(null));
        assertNull(YesNoUnknown.fromJson(NODES.nullNode()));
        assertEquals(YesNoUnknown.YES, YesNoUnknown.fromJson(NODES.booleanNode(true)));
        assertEquals(YesNoUnknown.NO, YesNoUnknown.fromJson(NODES.booleanNode(false)));
        assertEquals(YesNoUnknown.UNKNOWN, YesNoUnknown.fromJson(NODES.textNode("unknown")));
    }

    /** A typo must not be coerced to "No", which is what JsonNode.asBoolean() would do. */
    @Test
    public void rejectsAnythingElse() {
        for (JsonNode bad : List.<JsonNode>of(
                NODES.textNode("Unknown"),
                NODES.textNode("yes"),
                NODES.textNode("true"),
                NODES.textNode(""),
                NODES.numberNode(0),
                NODES.arrayNode())) {
            assertThrows(bad.toString(), IllegalArgumentException.class, () -> YesNoUnknown.fromJson(bad));
        }
    }

    @Test
    public void serializesToTheWireValue() throws Exception {
        assertEquals("true", MAPPER.writeValueAsString(YesNoUnknown.YES));
        assertEquals("false", MAPPER.writeValueAsString(YesNoUnknown.NO));
        assertEquals("\"unknown\"", MAPPER.writeValueAsString(YesNoUnknown.UNKNOWN));
    }

    @Test
    public void roundTripsThroughTheEntityColumns() {
        Mutation m = new Mutation();
        assertNull(m.getHomozygousLethal());
        for (YesNoUnknown answer : YesNoUnknown.values()) {
            m.setHomozygousLethal(answer);
            assertEquals(answer, m.getHomozygousLethal());
        }
        m.setHomozygousLethal(null);
        assertNull(m.getHomozygousLethal());
    }

    /** Changing an "unknown" answer to Yes must clear the flag, or the read would still say unknown. */
    @Test
    public void answeringAfterUnknownClearsIt() {
        Phenotype p = new Phenotype();
        p.setBackgroundDependent(YesNoUnknown.UNKNOWN);
        p.setBackgroundDependent(YesNoUnknown.YES);
        assertEquals(YesNoUnknown.YES, p.getBackgroundDependent());
    }

    @Test
    public void mutationFieldDescriptorRoundTripsAndRejects() {
        ZircMutationFormSchema.FieldDescriptor d = ZircMutationFormSchema.FIELDS.get("/homozygousLethal");
        Mutation m = new Mutation();

        d.write().accept(m, NODES.textNode("unknown"));
        assertEquals(NODES.textNode("unknown"), d.read().apply(m));

        d.write().accept(m, NODES.booleanNode(false));
        assertEquals(NODES.booleanNode(false), d.read().apply(m));

        assertThrows(IllegalArgumentException.class, () -> d.write().accept(m, NODES.textNode("maybe")));
        assertEquals("a rejected value leaves the stored answer alone",
                YesNoUnknown.NO, m.getHomozygousLethal());
    }

    @Test
    public void phenotypeFieldDescriptorRoundTrips() {
        ZircPhenotypeFormSchema.FieldDescriptor d = ZircPhenotypeFormSchema.FIELDS.get("/backgroundDependent");
        Phenotype p = new Phenotype();

        d.write().accept(p, NODES.textNode("unknown"));
        assertEquals(NODES.textNode("unknown"), d.read().apply(p));

        d.write().accept(p, NODES.nullNode());
        JsonNode cleared = d.read().apply(p);
        assertTrue(cleared == null || cleared.isNull());
    }
}
