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
import org.zfin.zirc.dto.GeneDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.service.ZircSubmissionService;

/**
 * Endpoints for the per-mutation gene collection. Mirrors
 * {@link ZircAssayApiController}: add lives under the parent mutation;
 * GET/PATCH/DELETE key off the gene id directly.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircGeneApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @GetMapping("/api/zirc/genes/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(ZircGeneFormSchema.schema(), ZircGeneFormSchema.uiSchema());
    }

    @GetMapping("/api/zirc/genes/{geneId}")
    public GeneDTO getGene(@PathVariable Long geneId) {
        return GeneDTO.of(zircSubmissionService.getRequiredGeneById(geneId));
    }

    @PatchMapping("/api/zirc/genes/{geneId}")
    public GeneDTO updateField(
            @PathVariable Long geneId,
            @Valid @RequestBody FieldUpdate update) {
        return GeneDTO.of(zircSubmissionService.updateGeneField(geneId, update));
    }

    @PostMapping("/api/zirc/mutations/{mutationId}/genes")
    @ResponseStatus(HttpStatus.CREATED)
    public MutationDTO addGene(@PathVariable Long mutationId) {
        return MutationDTO.of(zircSubmissionService.addGene(mutationId));
    }

    @DeleteMapping("/api/zirc/genes/{geneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGene(@PathVariable Long geneId) {
        zircSubmissionService.deleteGene(geneId);
    }
}
