package org.zfin.zirc.api.uischema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.With;

import java.util.List;

/**
 * The vocabulary of {@code options} keys understood by our renderers,
 * captured in one place. See {@code reference/zirc-architecture.md} §5
 * for what each one means and which renderers honor it.
 *
 * <p>Mutate via the Lombok-generated {@code withX} withers — each one returns a
 * new instance. {@code Options.of()} produces an empty instance to start from.
 *
 * <pre>{@code
 * Options.of()
 *     .withPlaceholder("e.g. zf123")
 *     .withHelpText("ZFIN allele designation; leave blank if not yet assigned.")
 * }</pre>
 *
 * <p>The fields are explicit (not a generic Map) so a typo on a key name
 * fails at compile time instead of silently being ignored at runtime.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@With
@Builder(access = AccessLevel.PRIVATE)
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
        // Per-field opt-out for the comments icon. Defaults to enabled
        // when null; set to {@code false} on fields where curator-authored
        // comments don't make sense (e.g. server-managed timestamps,
        // surrogate primary keys). Renderers check
        // {@code uischema.options?.comments !== false} to decide.
        Boolean comments,
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
        Boolean refreshesParent,
        // Label for the stringList widget's "+ Add …" button (e.g.
        // "+ Add publication"). Falls back to "+ Add" if unset.
        String addLabel,
        // Parallel to standardValues — display labels shown in the dropdown
        // when value tokens are not curator-facing (e.g. ["pcr_gel"] →
        // ["PCR + gel electrophoresis"]). If null, values are used as labels.
        List<String> standardLabels,
        // For selectWithOther — when true, suppress the "Other" option (and
        // its free-text input). Use for closed enums like assayType.
        Boolean noOther,
        // For the autoSize widget — the sibling field whose sequence length
        // this read-only control displays (e.g. lesionSizeBp derives from
        // deletedSequence). Mutually exclusive with constantValue.
        String sourceField,
        // For the autoSize widget — a fixed displayed value where the size is
        // definitional rather than measured (a point mutation is always 1 bp).
        // Mutually exclusive with sourceField.
        Integer constantValue,
        // For the vocabularySelect / vocabularyMultiSelect / aminoAcidChange
        // widgets — which controlled vocabulary to populate the dropdown
        // from. An mdcv_used_in value served by /api/zirc/vocabulary/{name};
        // see ZircVocabularyService for the served set. The stored value is
        // the term's ZDB ID, not its display name.
        String vocabulary,
        // For the nucleotideSequence widget — the accepted character set.
        // Input is uppercased and anything outside this set is dropped, so
        // a pasted FASTA record or numbered sequence normalizes to bases.
        // Defaults client-side to "ACGTN".
        String alphabet,
        // For the aminoAcidChange widget — the sibling fields it writes
        // alongside the one it is bound to (which holds the "from" residue).
        // Named rather than derived so the schema stays the single place
        // field names are declared.
        String toField,
        String positionField,
        // For the aminoAcidChange widget — the end of the position range.
        // Optional: omit it and the widget renders a single position box.
        String positionEndField
) {

    /**
     * An empty instance to start a wither chain from. Built via the private
     * Lombok builder so adding a component here costs nothing — the
     * alternative is a positional list of nulls that has to grow in lockstep
     * with the record header.
     */
    public static Options of() {
        return builder().build();
    }
}
