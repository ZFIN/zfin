package org.zfin.zirc.api.jsonschema;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Smoke test: confirms the typed JSON Schema records serialize to the
 * exact shapes used today by ZircFormSchema / ZircMutationFormSchema /
 * ZircAssayFormSchema. If a shape drifts, the migration in #94 would
 * silently change the wire format.
 */
public class JsonSchemaSerializationTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .build();

    @Test
    public void stringWithMaxLengthAndTitle() throws Exception {
        String json = MAPPER.writeValueAsString(StringSchema.of("Name", 255));
        assertEquals(
                "{\n" +
                "  \"maxLength\" : 255,\n" +
                "  \"title\" : \"Name\",\n" +
                "  \"type\" : [ \"string\", \"null\" ]\n" +
                "}",
                json);
    }

    @Test
    public void nullableStringEmitsArrayType() throws Exception {
        String json = MAPPER.writeValueAsString(StringSchema.nullable());
        assertEquals(
                "{\n" +
                "  \"type\" : [ \"string\", \"null\" ]\n" +
                "}",
                json);
    }

    @Test
    public void nullableBooleanEmitsArrayType() throws Exception {
        String json = MAPPER.writeValueAsString(BooleanSchema.nullable("Background Changeable"));
        assertEquals(
                "{\n" +
                "  \"title\" : \"Background Changeable\",\n" +
                "  \"type\" : [ \"boolean\", \"null\" ]\n" +
                "}",
                json);
    }

    @Test
    public void numberSchema() throws Exception {
        String json = MAPPER.writeValueAsString(NumberSchema.of());
        assertEquals(
                "{\n" +
                "  \"type\" : \"number\"\n" +
                "}",
                json);
    }

    @Test
    public void arrayWithItemsAndCaps() throws Exception {
        ArraySchema arr = new ArraySchema(
                "Mutations",
                NumberSchema.of(),
                5,
                null);
        String json = MAPPER.writeValueAsString(arr);
        assertEquals(
                "{\n" +
                "  \"items\" : {\n" +
                "    \"type\" : \"number\"\n" +
                "  },\n" +
                "  \"maxItems\" : 5,\n" +
                "  \"title\" : \"Mutations\",\n" +
                "  \"type\" : \"array\"\n" +
                "}",
                json);
    }

    @Test
    public void reasonsArrayWithOneOfConsts() throws Exception {
        // Mirrors the canonical-reasons array shape on the submission form.
        // NOTE: Jackson's SORT_PROPERTIES_ALPHABETICALLY appends method-
        // derived @JsonProperty accessors AFTER record components instead
        // of interleaving them alphabetically. So `uniqueItems` (a record
        // component) sorts BEFORE `type` (a method-derived property) even
        // though "type" < "uniqueItems" lexicographically. The Map-based
        // snapshot today emits {items, type, uniqueItems} (true
        // alphabetical); the record-based version emits
        // {items, uniqueItems, type}. JSON Forms reads by key name not
        // position so the wire-shape equivalence is fine, but the
        // snapshot baseline will need a one-time regenerate when #94
        // migrates the Map-based schema() to records.
        ArraySchema reasons = new ArraySchema(
                null,
                StringSchema.withOneOf(List.of(
                        new ConstSchema("frequently_requested", "Currently frequently requested"),
                        new ConstSchema("expect_high_demand",   "Expect high demand"))),
                null,
                Boolean.TRUE);
        String json = MAPPER.writeValueAsString(reasons);
        assertEquals(
                "{\n" +
                "  \"items\" : {\n" +
                "    \"oneOf\" : [ {\n" +
                "      \"const\" : \"frequently_requested\",\n" +
                "      \"title\" : \"Currently frequently requested\"\n" +
                "    }, {\n" +
                "      \"const\" : \"expect_high_demand\",\n" +
                "      \"title\" : \"Expect high demand\"\n" +
                "    } ],\n" +
                "    \"type\" : \"string\"\n" +
                "  },\n" +
                "  \"uniqueItems\" : true,\n" +
                "  \"type\" : \"array\"\n" +
                "}",
                json);
    }

    @Test
    public void objectWithNestedProperties() throws Exception {
        // Acceptance section shape.
        Map<String, JsonSchema> props = new LinkedHashMap<>();
        props.put("reasons", ArraySchema.of(StringSchema.nullable()));
        props.put("reasonsOther", StringSchema.of("Other reason", 2000));
        ObjectSchema acceptance = ObjectSchema.of("Acceptance Reasons", props);
        String json = MAPPER.writeValueAsString(acceptance);
        assertEquals(
                "{\n" +
                "  \"properties\" : {\n" +
                "    \"reasons\" : {\n" +
                "      \"items\" : {\n" +
                "        \"type\" : [ \"string\", \"null\" ]\n" +
                "      },\n" +
                "      \"type\" : \"array\"\n" +
                "    },\n" +
                "    \"reasonsOther\" : {\n" +
                "      \"maxLength\" : 2000,\n" +
                "      \"title\" : \"Other reason\",\n" +
                "      \"type\" : [ \"string\", \"null\" ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"title\" : \"Acceptance Reasons\",\n" +
                "  \"type\" : \"object\"\n" +
                "}",
                json);
    }

    @Test
    public void constSchemaHasNoType() throws Exception {
        // ConstSchema entries inside oneOf don't carry a "type" field.
        String json = MAPPER.writeValueAsString(
                new ConstSchema("other", "Other"));
        assertEquals(
                "{\n" +
                "  \"const\" : \"other\",\n" +
                "  \"title\" : \"Other\"\n" +
                "}",
                json);
    }
}
