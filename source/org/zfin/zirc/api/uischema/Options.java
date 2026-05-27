package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * The vocabulary of {@code options} keys understood by our renderers,
 * captured in one place. See {@code reference/zirc-architecture.md} §5
 * for what each one means and which renderers honor it.
 *
 * <p>Mutate via the fluent withers — each one returns a new instance.
 * {@code Options.of()} produces an empty instance to start from.
 *
 * <pre>{@code
 * Options.of()
 *     .placeholder("e.g. zf123")
 *     .helpText("ZFIN allele designation; leave blank if not yet assigned.")
 * }</pre>
 *
 * <p>The fields are explicit (not a generic Map) so a typo on a key name
 * fails at compile time instead of silently being ignored at runtime.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Options(
        // Layout-on-Group hint: "plain" drops the table wrapper.
        String layout,
        // Per-control widget dispatch (e.g. "yesNoRadio", "selectWithOther").
        String widget,
        // Multi-line textarea on a string Control.
        Boolean multi,
        // For selectWithOther / multipleChoiceWithOther — canonical values.
        List<String> standardValues,
        // Curator-facing UX metadata.
        String placeholder,
        String helpText,
        String infoHref,
        String suffix,
        // Override label-from-property-title.
        String label,
        // Autocomplete widget: which /api/zirc/autocomplete/{...}
        // endpoint to hit. One of "markers", "features", "persons".
        // Defaults client-side to "markers".
        String searchEndpoint,
        // Autocomplete typeGroup filter (applies to the markers endpoint;
        // ignored elsewhere). Validated server-side against
        // {@code Marker.TypeGroup}; e.g. "GENEDOM" for the gene picker.
        String typeGroup,
        // The widget manages its own writes (uploads/deletes/add-row via
        // dedicated POST/DELETE endpoints), so the autosave loop must skip
        // this Control's path. Set on the self-managed list widgets
        // (mutationsList, assaysList, attachmentsList, …). The editor
        // derives its autosave skip-set + mirror-sync keys from these.
        Boolean managesOwnPersistence,
        // Editing this field changes what the parent's collapsed card
        // displays, so a successful PATCH on it should invalidate the
        // parent query. Set on the field that feeds the parent card label
        // (the type discriminator, or the gene's marker id, or the
        // phenotype description). Not always a type discriminator.
        Boolean refreshesParent
) {

    public static Options of() {
        return new Options(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public Options layout(String v)         { return new Options(v, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options widget(String v)         { return new Options(layout, v, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options multi(boolean v)         { return new Options(layout, widget, v, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options standardValues(List<String> v) { return new Options(layout, widget, multi, v, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options placeholder(String v)    { return new Options(layout, widget, multi, standardValues, v, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options helpText(String v)       { return new Options(layout, widget, multi, standardValues, placeholder, v, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options infoHref(String v)       { return new Options(layout, widget, multi, standardValues, placeholder, helpText, v, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options suffix(String v)         { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, v, label, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options label(String v)          { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, v, searchEndpoint, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options searchEndpoint(String v) { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, v, typeGroup, managesOwnPersistence, refreshesParent); }
    public Options typeGroup(String v)      { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, v, managesOwnPersistence, refreshesParent); }
    public Options managesOwnPersistence(boolean v) { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, v, refreshesParent); }
    public Options refreshesParent(boolean v)       { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup, managesOwnPersistence, v); }
}
