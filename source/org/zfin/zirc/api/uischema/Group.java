package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * A labelled section. Renders as {@code <section class="section"><h2>label</h2>...</section>}
 * via the React {@code SectionRenderer}. Two relevant variants:
 *
 * <ul>
 *   <li>{@code options.layout = "plain"} drops the default table wrapper —
 *       used for list-of-cards content like Mutations and Genotyping Assays.
 *   <li>A non-null {@link Rule} gates the whole group's visibility on
 *       a discriminator (e.g., the assay-type field matrix uses one
 *       SHOW rule per cluster).
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Group(
        String label,
        List<UiSchemaElement> elements,
        Options options,
        Rule rule
) implements UiSchemaElement {

    /** Two-arg shorthand for the common case (no options, no rule). */
    public static Group of(String label, List<UiSchemaElement> elements) {
        return new Group(label, elements, null, null);
    }

    @Override
    public String type() {
        return "Group";
    }
}
