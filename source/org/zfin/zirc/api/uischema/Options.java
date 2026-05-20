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
        String typeGroup
) {

    public static Options of() {
        return new Options(null, null, null, null, null, null, null, null, null, null, null);
    }

    public Options layout(String v)         { return new Options(v, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options widget(String v)         { return new Options(layout, v, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options multi(boolean v)         { return new Options(layout, widget, v, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options standardValues(List<String> v) { return new Options(layout, widget, multi, v, placeholder, helpText, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options placeholder(String v)    { return new Options(layout, widget, multi, standardValues, v, helpText, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options helpText(String v)       { return new Options(layout, widget, multi, standardValues, placeholder, v, infoHref, suffix, label, searchEndpoint, typeGroup); }
    public Options infoHref(String v)       { return new Options(layout, widget, multi, standardValues, placeholder, helpText, v, suffix, label, searchEndpoint, typeGroup); }
    public Options suffix(String v)         { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, v, label, searchEndpoint, typeGroup); }
    public Options label(String v)          { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, v, searchEndpoint, typeGroup); }
    public Options searchEndpoint(String v) { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, v, typeGroup); }
    public Options typeGroup(String v)      { return new Options(layout, widget, multi, standardValues, placeholder, helpText, infoHref, suffix, label, searchEndpoint, v); }
}
