package org.zfin.zirc.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.zirc.dto.FieldUpdate;
import org.zfin.zirc.dto.FormSchemaDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.dto.PhenotypeDTO;
import org.zfin.zirc.entity.PhenotypeFile;
import org.zfin.zirc.service.ZircSubmissionService;

import java.io.File;
import java.io.IOException;

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

    /**
     * Multipart upload (ZFIN-10449). Returns the refreshed PhenotypeDTO so the
     * client updates its attachments list and its React Query cache in one
     * round trip — the form mirror-syncs the array out of this response rather
     * than PATCHing it.
     */
    @PostMapping(
            value = "/api/zirc/phenotypes/{phenotypeId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhenotypeDTO uploadAttachment(
            @PathVariable Long phenotypeId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return PhenotypeDTO.of(zircSubmissionService.storePhenotypeAttachment(phenotypeId, file));
    }

    @DeleteMapping("/api/zirc/phenotypes/attachments/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@PathVariable Long fileId) {
        zircSubmissionService.deletePhenotypeAttachment(fileId);
    }

    /**
     * Stream the bytes, for the browser's image preview and for download.
     * Content-Disposition carries the original filename, not the on-disk name,
     * which is prefixed and id-stamped by the server.
     */
    @GetMapping("/api/zirc/phenotypes/attachments/{fileId}/content")
    public ResponseEntity<FileSystemResource> getAttachmentContent(
            @PathVariable Long fileId) {
        PhenotypeFile meta = zircSubmissionService.getRequiredPhenotypeFile(fileId);
        File onDisk = zircSubmissionService.resolveAttachmentPath(meta);
        MediaType type = meta.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(meta.getContentType());
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(meta.getOriginalFilename() == null
                                        ? "file" : meta.getOriginalFilename())
                                .build().toString())
                .body(new FileSystemResource(onDisk));
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
