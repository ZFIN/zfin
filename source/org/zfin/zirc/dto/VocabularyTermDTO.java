package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;

/**
 * One term from a {@code mutation_detail_controlled_vocabulary} pick list,
 * as served by {@code /api/zirc/vocabulary/{name}}.
 *
 * <p>{@code id} is the term's ZDB ID — the value the form stores, since a
 * display name can be edited without orphaning saved data. {@code label} is
 * what the dropdown shows. {@code abbreviation} is populated only for the
 * amino-acid vocabulary (the single-letter code), so the picker can render
 * "Ala [A]" the way the curation interface does; it is null elsewhere.
 */
public record VocabularyTermDTO(
        @NotNull String id,
        @NotNull String label,
        String abbreviation) {
}
