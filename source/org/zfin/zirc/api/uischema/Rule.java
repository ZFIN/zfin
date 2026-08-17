package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Conditional rule on a uiSchema element. Two factory shortcuts cover
 * the patterns we actually use:
 *
 * <pre>{@code
 * Rule.showWhenTrue("#/properties/homozygousLethal")
 * Rule.showWhenIn("#/properties/assayType", "PCR", "RFLP", "dCAPS")
 * }</pre>
 *
 * <p>Anything more complex (HIDE effect, ENABLE/DISABLE, regex match,
 * etc.) constructs a {@link RuleCondition} directly — the schema field
 * of {@link RuleCondition} is intentionally an open {@code Map} since
 * JSON Forms rule conditions accept any JSON Schema fragment.
 *
 * <p>Conditions over more than one field use {@link #showWhenAll}, which
 * emits JSON Forms' {@code {type: "AND", conditions: [...]}} shape. Nesting
 * ruled Groups would be the obvious alternative but does not work here:
 * SectionRenderer wraps each Group's children in a {@code <table><tbody>},
 * so a Group inside a Group puts a {@code <section>} inside a
 * {@code <tbody>}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Rule(Effect effect, Condition condition) {

    public enum Effect { SHOW, HIDE, ENABLE, DISABLE }

    /** Either a single scoped test or a composite over several. */
    public sealed interface Condition permits RuleCondition, CompositeCondition {}

    /**
     * Open-ended JSON Schema fragment used to test the scoped value.
     * Common shapes: {@code Map.of("const", true)},
     * {@code Map.of("enum", List.of(...))}, {@code Map.of("pattern", "...")}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RuleCondition(String scope, Map<String, Object> schema)
            implements Condition {}

    /**
     * JSON Forms' composable condition — {@code type} is {@code "AND"} or
     * {@code "OR"}, and each entry is itself a condition.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CompositeCondition(String type, List<Condition> conditions)
            implements Condition {}

    /** SHOW when the scoped value equals boolean true. */
    public static Rule showWhenTrue(String scope) {
        return new Rule(Effect.SHOW,
                new RuleCondition(scope, Map.of("const", true)));
    }

    /**
     * HIDE when the scoped value equals boolean true; equivalent to
     * SHOW-when-not-true. The complement of {@link #showWhenTrue} is
     * the common case for "render alternative widget when flag is off"
     * (e.g. plain input + autocomplete on the same scope).
     */
    public static Rule hideWhenTrue(String scope) {
        return new Rule(Effect.HIDE,
                new RuleCondition(scope, Map.of("const", true)));
    }

    /** SHOW when the scoped value is one of the listed values. */
    public static Rule showWhenIn(String scope, String... values) {
        return new Rule(Effect.SHOW,
                new RuleCondition(scope, Map.of("enum", List.of(values))));
    }

    /** SHOW when the scoped value is one of the listed values. */
    public static Rule showWhenIn(String scope, List<String> values) {
        return new Rule(Effect.SHOW, in(scope, values));
    }

    /** SHOW only when every supplied condition holds. */
    public static Rule showWhenAll(Condition... conditions) {
        return new Rule(Effect.SHOW,
                new CompositeCondition("AND", List.of(conditions)));
    }

    /** Condition building block: the scoped value is one of {@code values}. */
    public static Condition in(String scope, List<String> values) {
        return new RuleCondition(scope, Map.of("enum", values));
    }

    /** Condition building block: the scoped value is boolean true. */
    public static Condition isTrue(String scope) {
        return new RuleCondition(scope, Map.of("const", true));
    }

    /**
     * Condition building block: the scoped array contains {@code value}.
     *
     * <p>JSON Schema's {@code contains}, which JSON Forms evaluates through
     * Ajv like any other schema-based condition. Lets a checklist reveal one
     * follow-up field per box ticked.
     *
     * <p>{@code type: "array"} is not decoration. {@code contains} applies
     * only to arrays and is ignored for anything else, so against an absent
     * or null value the condition would pass and reveal every follow-up at
     * once. Requiring the type makes the absent case fail closed.
     */
    public static Condition arrayContains(String scope, String value) {
        return new RuleCondition(scope, Map.of(
                "type", "array",
                "contains", Map.of("const", value)));
    }
}
