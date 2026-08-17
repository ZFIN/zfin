package org.zfin.zirc.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.zirc.dto.VocabularyTermDTO;
import org.zfin.zirc.service.ZircVocabularyService;

import java.util.List;

/**
 * Controlled-vocabulary pick lists for the form's select widgets, read
 * live from {@code mutation_detail_controlled_vocabulary} so ZIRC and the
 * curation interface stay in agreement about both membership and order.
 *
 * <p>{@code name} is an {@code mdcv_used_in} discriminator value — see
 * {@link ZircVocabularyService#vocabularyNames()} for the served set. An
 * unknown name is a 404 problem detail, not an empty list, so a typo in a
 * uiSchema {@code options.vocabulary} fails loudly instead of rendering a
 * silently empty dropdown.
 */
@RestController
@RequestMapping(path = "/api/zirc/vocabulary", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircVocabularyApiController {

    @Autowired
    private ZircVocabularyService vocabularyService;

    @GetMapping("/{name}")
    public List<VocabularyTermDTO> terms(@PathVariable("name") String name) {
        return vocabularyService.terms(name);
    }
}
