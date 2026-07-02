package org.zfin.zirc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.zirc.dto.AutocompleteItemDTO;
import org.zfin.zirc.service.ZircAutocompleteService;

import java.util.List;

/**
 * Type-ahead lookups used by the M5–M7 form sections (linked features,
 * genes, lesions) and the existing add-submitter modal.
 *
 * <p>Endpoints under {@code /api/zirc/autocomplete/}; each takes a
 * {@code term} query parameter (empty/missing returns an empty list)
 * and returns up to 20 {@link AutocompleteItemDTO} rows.
 *
 * <p>Passing {@code exactMatch=true} switches a lookup from substring
 * search to a by-ZDB-ID resolution that returns the single matching row
 * (or an empty list). The form's record chip uses this to show the symbol
 * for an already-stored id and to validate a directly-typed/pasted id.
 */
@RestController
@RequestMapping(path = "/api/zirc/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircAutocompleteApiController {

    @Autowired
    private ZircAutocompleteService autocompleteService;

    @GetMapping("/markers")
    public List<AutocompleteItemDTO> searchMarkers(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "typeGroup", required = false) String typeGroup,
            @RequestParam(value = "exactMatch", required = false, defaultValue = "false") boolean exactMatch) {
        return autocompleteService.searchMarkers(term, typeGroup, exactMatch);
    }

    @GetMapping("/features")
    public List<AutocompleteItemDTO> searchFeatures(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "exactMatch", required = false, defaultValue = "false") boolean exactMatch) {
        return autocompleteService.searchFeatures(term, exactMatch);
    }

    @GetMapping("/persons")
    public List<AutocompleteItemDTO> searchPersons(
            @RequestParam(value = "term", required = false) String term,
            @RequestParam(value = "exactMatch", required = false, defaultValue = "false") boolean exactMatch) {
        return autocompleteService.searchPersons(term, exactMatch);
    }

    /** Persons restricted to PI-level lab positions (PI/Director, Co-PI). */
    @GetMapping("/pis")
    public List<AutocompleteItemDTO> searchPIs(
            @RequestParam(value = "term", required = false) String term) {
        return autocompleteService.searchPIs(term);
    }
}
