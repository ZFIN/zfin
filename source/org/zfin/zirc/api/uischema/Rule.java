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
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Rule(Effect effect, RuleCondition condition) {

    public enum Effect { SHOW, HIDE, ENABLE, DISABLE }

    /**
     * Open-ended JSON Schema fragment used to test the scoped value.
     * Common shapes: {@code Map.of("const", true)},
     * {@code Map.of("enum", List.of(...))}, {@code Map.of("pattern", "...")}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RuleCondition(String scope, Map<String, Object> schema) {}

    /** SHOW when the scoped value equals boolean true. */
    public static Rule showWhenTrue(String scope) {
        return new Rule(Effect.SHOW,
                new RuleCondition(scope, Map.of("const", true)));
    }

    /** SHOW when the scoped value is one of the listed values. */
    public static Rule showWhenIn(String scope, String... values) {
        return new Rule(Effect.SHOW,
                new RuleCondition(scope, Map.of("enum", List.of(values))));
    }

    /** SHOW when the scoped value is one of the listed values. */
    public static Rule showWhenIn(String scope, List<String> values) {
        return new Rule(Effect.SHOW,
                new RuleCondition(scope, Map.of("enum", values)));
    }
}
