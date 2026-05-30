package org.zfin.zirc.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.zirc.dto.FieldUpdate;
import org.zfin.zirc.dto.FormSchemaDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.service.ZircSubmissionService;

/**
 * JSON API for per-mutation editing. Mirrors
 * {@link ZircSubmissionApiController} for the mutation aggregate: schema,
 * fetch, and field-path PATCH. Each PATCH writes one audit-log row keyed
 * by mutation id.
 */
@RestController
@RequestMapping(path = "/api/zirc/mutations", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircMutationApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @GetMapping("/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(
                ZircMutationFormSchema.schema(),
                ZircMutationFormSchema.uiSchema());
    }

    @GetMapping("/{mutationId}")
    public MutationDTO getMutation(@PathVariable Long mutationId) {
        return MutationDTO.of(zircSubmissionService.getRequiredMutationById(mutationId));
    }

    @PatchMapping("/{mutationId}")
    public MutationDTO updateField(
            @PathVariable Long mutationId,
            @Valid @RequestBody FieldUpdate update) {
        return MutationDTO.of(zircSubmissionService.updateMutationField(mutationId, update));
    }
}
