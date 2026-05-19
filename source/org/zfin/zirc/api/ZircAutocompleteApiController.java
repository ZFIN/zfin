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
 * <p>Three endpoints under {@code /api/zirc/autocomplete/}; each takes
 * a {@code term} query parameter (empty/missing returns an empty list)
 * and returns up to 20 {@link AutocompleteItemDTO} rows.
 */
@RestController
@RequestMapping(path = "/api/zirc/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircAutocompleteApiController {

    @Autowired
    private ZircAutocompleteService autocompleteService;

    @GetMapping("/markers")
    public List<AutocompleteItemDTO> searchMarkers(
            @RequestParam(value = "term", required = false) String term) {
        return autocompleteService.searchMarkers(term);
    }

    @GetMapping("/features")
    public List<AutocompleteItemDTO> searchFeatures(
            @RequestParam(value = "term", required = false) String term) {
        return autocompleteService.searchFeatures(term);
    }

    @GetMapping("/persons")
    public List<AutocompleteItemDTO> searchPersons(
            @RequestParam(value = "term", required = false) String term) {
        return autocompleteService.searchPersons(term);
    }
}
