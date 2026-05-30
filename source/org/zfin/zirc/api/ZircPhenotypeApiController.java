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
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.dto.PhenotypeDTO;
import org.zfin.zirc.service.ZircSubmissionService;

/**
 * Endpoints for the per-mutation phenotype collection (M8.1). Mirrors
 * the lesion + gene + assay controllers: add lives under the parent
 * mutation, GET/PATCH/DELETE key off the phenotype id directly.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircPhenotypeApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @GetMapping("/api/zirc/phenotypes/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(ZircPhenotypeFormSchema.schema(), ZircPhenotypeFormSchema.uiSchema());
    }

    @GetMapping("/api/zirc/phenotypes/{phenotypeId}")
    public PhenotypeDTO getPhenotype(@PathVariable Long phenotypeId) {
        return PhenotypeDTO.of(zircSubmissionService.getRequiredPhenotypeById(phenotypeId));
    }

    @PatchMapping("/api/zirc/phenotypes/{phenotypeId}")
    public PhenotypeDTO updateField(
            @PathVariable Long phenotypeId,
            @Valid @RequestBody FieldUpdate update) {
        return PhenotypeDTO.of(zircSubmissionService.updatePhenotypeField(phenotypeId, update));
    }

    @PostMapping("/api/zirc/mutations/{mutationId}/phenotypes")
    @ResponseStatus(HttpStatus.CREATED)
    public MutationDTO addPhenotype(@PathVariable Long mutationId) {
        return MutationDTO.of(zircSubmissionService.addPhenotype(mutationId));
    }

    @DeleteMapping("/api/zirc/phenotypes/{phenotypeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhenotype(@PathVariable Long phenotypeId) {
        zircSubmissionService.deletePhenotype(phenotypeId);
    }
}
