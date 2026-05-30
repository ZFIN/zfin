package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Smoke test: confirms the typed uiSchema tree serializes to the exact
 * JSON shape JSON Forms expects. If this drifts, the migration in #93
 * would silently produce a different wire format and the form would
 * stop rendering correctly on the client.
 */
public class UiSchemaSerializationTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    @Test
    public void emptyVerticalLayoutSerializes() throws Exception {
        String json = MAPPER.writeValueAsString(new VerticalLayout(List.of()));
        assertEquals(
                "{\n" +
                "  \"elements\" : [ ],\n" +
                "  \"type\" : \"VerticalLayout\"\n" +
                "}",
                json);
    }

    @Test
    public void controlWithEmptyOptionsOmitsOptions() throws Exception {
        String json = MAPPER.writeValueAsString(Control.of("#/properties/name"));
        // Confirm @JsonInclude(NON_NULL) drops absent fields
        assertEquals(
                "{\n" +
                "  \"scope\" : \"#/properties/name\",\n" +
                "  \"type\" : \"Control\"\n" +
                "}",
                json);
    }

    @Test
    public void controlWithMetadataOptionsSerializesAlphabetized() throws Exception {
        Control c = new Control("#/properties/forwardPrimer",
                Options.of()
                        .placeholder("5′ → 3′")
                        .helpText("Forward primer sequence"),
                null);
        String json = MAPPER.writeValueAsString(c);
        assertEquals(
                "{\n" +
                "  \"options\" : {\n" +
                "    \"helpText\" : \"Forward primer sequence\",\n" +
                "    \"placeholder\" : \"5′ → 3′\"\n" +
                "  },\n" +
                "  \"scope\" : \"#/properties/forwardPrimer\",\n" +
                "  \"type\" : \"Control\"\n" +
                "}",
                json);
    }

    @Test
    public void groupWithShowWhenInRuleSerializes() throws Exception {
        Group g = new Group("PCR Primers",
                List.of(Control.of("#/properties/forwardPrimer")),
                null,
                Rule.showWhenIn("#/properties/assayType", "PCR", "RFLP"));
        String json = MAPPER.writeValueAsString(g);
        assertEquals(
                "{\n" +
                "  \"elements\" : [ {\n" +
                "    \"scope\" : \"#/properties/forwardPrimer\",\n" +
                "    \"type\" : \"Control\"\n" +
                "  } ],\n" +
                "  \"label\" : \"PCR Primers\",\n" +
                "  \"rule\" : {\n" +
                "    \"condition\" : {\n" +
                "      \"schema\" : {\n" +
                "        \"enum\" : [ \"PCR\", \"RFLP\" ]\n" +
                "      },\n" +
                "      \"scope\" : \"#/properties/assayType\"\n" +
                "    },\n" +
                "    \"effect\" : \"SHOW\"\n" +
                "  },\n" +
                "  \"type\" : \"Group\"\n" +
                "}",
                json);
    }

    @Test
    public void rulesEnumSerializesAsName() throws Exception {
        Rule r = new Rule(Rule.Effect.HIDE,
                new Rule.RuleCondition("#/properties/x", Map.of("const", false)));
        String json = MAPPER.writeValueAsString(r);
        // "HIDE" not "hide" — JSON Forms expects the uppercase form.
        assertEquals(
                "{\n" +
                "  \"condition\" : {\n" +
                "    \"schema\" : {\n" +
                "      \"const\" : false\n" +
                "    },\n" +
                "    \"scope\" : \"#/properties/x\"\n" +
                "  },\n" +
                "  \"effect\" : \"HIDE\"\n" +
                "}",
                json);
    }
}
