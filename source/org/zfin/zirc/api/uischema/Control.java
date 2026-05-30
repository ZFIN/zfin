package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A single form control bound to a JSON Pointer scope into the form's
 * data. Scope syntax is JSON Forms' own:
 * {@code #/properties/foo} for top-level fields,
 * {@code #/properties/background/properties/maternalBackground}
 * for nested ones.
 *
 * <p>The {@link Options#widget()} key drives renderer dispatch on the
 * client (e.g. {@code "yesNoRadio"}, {@code "selectWithOther"},
 * {@code "stringList"}, {@code "mutationsList"}). The remaining
 * options carry curator-facing render metadata —
 * placeholder/helpText/infoHref/suffix.
 *
 * <p>A non-null {@link Rule} gates this single control's visibility;
 * use a {@link Group} rule when several controls share the same
 * condition.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Control(
        String scope,
        Options options,
        Rule rule
) implements UiSchemaElement {

    /** Common shorthand: just a scope, no options, no rule. */
    public static Control of(String scope) {
        return new Control(scope, null, null);
    }

    @Override
    public String type() {
        return "Control";
    }
}
