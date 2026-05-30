package org.zfin.zirc.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.zirc.dto.FieldUpdate;
import org.zfin.zirc.dto.FormSchemaDTO;
import org.zfin.zirc.dto.LesionDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.service.ZircSubmissionService;

/**
 * Endpoints for the per-mutation lesion collection (M7.1). Mirrors the
 * gene + assay controllers exactly: add lives under the parent
 * mutation, GET/PATCH/DELETE key off the lesion id directly.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircLesionApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @GetMapping("/api/zirc/lesions/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(ZircLesionFormSchema.schema(), ZircLesionFormSchema.uiSchema());
    }

    @GetMapping("/api/zirc/lesions/{lesionId}")
    public LesionDTO getLesion(@PathVariable Long lesionId) {
        return LesionDTO.of(zircSubmissionService.getRequiredLesionById(lesionId));
    }

    @PatchMapping("/api/zirc/lesions/{lesionId}")
    public LesionDTO updateField(
            @PathVariable Long lesionId,
            @Valid @RequestBody FieldUpdate update) {
        return LesionDTO.of(zircSubmissionService.updateLesionField(lesionId, update));
    }

    @PostMapping("/api/zirc/mutations/{mutationId}/lesions")
    @ResponseStatus(HttpStatus.CREATED)
    public MutationDTO addLesion(@PathVariable Long mutationId) {
        return MutationDTO.of(zircSubmissionService.addLesion(mutationId));
    }

    @DeleteMapping("/api/zirc/lesions/{lesionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesion(@PathVariable Long lesionId) {
        zircSubmissionService.deleteLesion(lesionId);
    }
}
