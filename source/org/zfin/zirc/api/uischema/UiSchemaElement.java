package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sealed root of the JSON Forms uiSchema element tree.
 *
 * <p>Each concrete subtype exposes a constant {@link #type()} string —
 * "VerticalLayout", "Group", "Control" — that Jackson serializes as a
 * regular property. JSON Forms reads it as a discriminator on the
 * client. Using a regular field (instead of {@code @JsonTypeInfo}'s
 * injection) keeps the property participating in
 * {@code SORT_PROPERTIES_ALPHABETICALLY}, which is what keeps the
 * snapshot test wire-equivalent across the Map → record migration.
 *
 * <p>Three concrete subtypes cover everything our forms use today:
 *
 * <ul>
 *   <li>{@link VerticalLayout} — top-level container</li>
 *   <li>{@link Group} — a labelled section</li>
 *   <li>{@link Control} — a single field bound to a JSON Pointer scope</li>
 * </ul>
 *
 * <p>JSON Forms has more layout types (HorizontalLayout, Categorization);
 * add them to the sealed set when a form needs them. Keeping the
 * permits clause tight catches "I meant to write a Group but typed the
 * wrong name" at compile time.
 *
 * <p>Note: we don't deserialize uiSchema back from JSON (the React client
 * is the only consumer), so polymorphic deserialization machinery isn't
 * needed.
 */
public sealed interface UiSchemaElement
        permits VerticalLayout, Group, Control {

    /** Discriminator value — "VerticalLayout", "Group", or "Control". */
    @JsonProperty("type")
    String type();
}
