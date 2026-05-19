package org.zfin.zirc.dto;

/**
 * One row in an autocomplete result list. {@code label} is the display
 * string shown in the dropdown ("nasl1 (ZDB-GENE-040426-1102)");
 * {@code value} is the canonical id stored back into the form. Both
 * fields are non-null. Used by the React {@code AutocompleteRenderer}
 * with three endpoints under {@code /api/zirc/autocomplete/*}.
 */
public record AutocompleteItemDTO(String label, String value) {
}
