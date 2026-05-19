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
import org.zfin.zirc.dto.LineSubmissionDTO;
import org.zfin.zirc.dto.LinkedFeatureDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.service.ZircSubmissionService;

@RestController
@RequestMapping(path = "/api/zirc", produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircSubmissionApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @GetMapping("/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(ZircFormSchema.schema(), ZircFormSchema.uiSchema());
    }

    @PostMapping("/line-submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public LineSubmissionDTO createLineSubmission() {
        return LineSubmissionDTO.of(zircSubmissionService.createDraftForCurrentUser());
    }

    @GetMapping("/line-submissions/{zdbID}")
    public LineSubmissionDTO getLineSubmission(@PathVariable String zdbID) {
        return LineSubmissionDTO.of(zircSubmissionService.getRequiredLineSubmission(zdbID));
    }

    /**
     * Single field change against the form schema. The path is checked against
     * {@link ZircFormSchema#FIELDS}; unknown paths reject with 400.
     */
    @PatchMapping("/line-submissions/{zdbID}")
    public LineSubmissionDTO updateField(
            @PathVariable String zdbID,
            @Valid @RequestBody FieldUpdate update) {
        return LineSubmissionDTO.of(zircSubmissionService.updateField(zdbID, update));
    }

    @PostMapping("/line-submissions/{zdbID}/mutations")
    @ResponseStatus(HttpStatus.CREATED)
    public MutationDTO addMutation(@PathVariable String zdbID) {
        return MutationDTO.of(zircSubmissionService.addMutation(zdbID));
    }

    @DeleteMapping("/line-submissions/{zdbID}/mutations/{mutationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMutation(@PathVariable String zdbID, @PathVariable Long mutationId) {
        zircSubmissionService.deleteMutation(zdbID, mutationId);
    }

    // ─── Linked Features (M5.3) ─────────────────────────────────────────────

    /**
     * Add a linkage between two mutations on this submission. The body
     * carries the pair so the URL doesn't have to commit to an order;
     * the service normalizes to (min, max) before save.
     */
    public record AddLinkedFeatureRequest(Long mutationAId, Long mutationBId) {}

    @PostMapping("/line-submissions/{zdbID}/linked-features")
    @ResponseStatus(HttpStatus.CREATED)
    public LineSubmissionDTO addLinkedFeature(
            @PathVariable String zdbID,
            @RequestBody AddLinkedFeatureRequest body) {
        return LineSubmissionDTO.of(
                zircSubmissionService.addLinkedFeature(zdbID, body.mutationAId(), body.mutationBId()));
    }

    @DeleteMapping("/line-submissions/{zdbID}/linked-features/{aId}/{bId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLinkedFeature(
            @PathVariable String zdbID,
            @PathVariable Long aId,
            @PathVariable Long bId) {
        zircSubmissionService.deleteLinkedFeature(zdbID, aId, bId);
    }

    @PatchMapping("/line-submissions/{zdbID}/linked-features/{aId}/{bId}")
    public LinkedFeatureDTO updateLinkedFeatureField(
            @PathVariable String zdbID,
            @PathVariable Long aId,
            @PathVariable Long bId,
            @Valid @RequestBody FieldUpdate update) {
        return LinkedFeatureDTO.of(
                zircSubmissionService.updateLinkedFeatureField(zdbID, aId, bId, update));
    }
}
