package org.zfin.zirc.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
import org.zfin.zirc.dto.AssayDTO;
import org.zfin.zirc.dto.FieldUpdate;
import org.zfin.zirc.dto.FormSchemaDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.entity.GenotypingAssayFile;
import org.zfin.zirc.service.ZircSubmissionService;

import java.io.File;
import java.io.IOException;

/**
 * Endpoints for the genotyping-assay collection under a mutation.
 *
 * <p>Add lives under the parent: {@code POST /api/zirc/mutations/{mutationId}/assays}
 * returns the updated MutationDTO so the React Query cache can
 * refresh in one round trip (matches how POST /mutations works for the
 * submission aggregate).
 *
 * <p>Delete keys directly off the assay id since the UI doesn't carry the
 * parent mutation in the URL after the row is rendered.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ZircAssayApiController {

    @Autowired
    private ZircSubmissionService zircSubmissionService;

    @PostMapping("/api/zirc/mutations/{mutationId}/assays")
    public MutationDTO addAssay(@PathVariable Long mutationId) {
        return MutationDTO.of(zircSubmissionService.addAssay(mutationId));
    }

    @DeleteMapping("/api/zirc/assays/{assayId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssay(@PathVariable Long assayId) {
        zircSubmissionService.deleteAssay(assayId);
    }

    @GetMapping("/api/zirc/assays/form-schema")
    public FormSchemaDTO getFormSchema() {
        return new FormSchemaDTO(
                ZircAssayFormSchema.schema(),
                ZircAssayFormSchema.uiSchema());
    }

    @GetMapping("/api/zirc/assays/{assayId}")
    public AssayDTO getAssay(@PathVariable Long assayId) {
        return AssayDTO.of(zircSubmissionService.getRequiredAssayById(assayId));
    }

    @PatchMapping("/api/zirc/assays/{assayId}")
    public AssayDTO updateField(
            @PathVariable Long assayId,
            @Valid @RequestBody FieldUpdate update) {
        return AssayDTO.of(zircSubmissionService.updateAssayField(assayId, update));
    }

    /**
     * Multipart upload — returns the refreshed AssayDTO so the client
     * can update both its attachments list and the local React Query cache
     * in one round trip.
     */
    @PostMapping(
            value = "/api/zirc/assays/{assayId}/attachments",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssayDTO uploadAttachment(
            @PathVariable Long assayId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return AssayDTO.of(zircSubmissionService.storeAttachment(assayId, file));
    }

    @DeleteMapping("/api/zirc/assays/attachments/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttachment(@PathVariable Long fileId) {
        zircSubmissionService.deleteAttachment(fileId);
    }

    /**
     * Stream the file contents. Browser uses this to render image previews
     * and to download via {@code &lt;a download&gt;}. Content-Disposition uses
     * the original filename, not the on-disk name.
     */
    @GetMapping("/api/zirc/assays/attachments/{fileId}/content")
    public ResponseEntity<FileSystemResource> getAttachmentContent(
            @PathVariable Long fileId,
            HttpServletResponse response) {
        GenotypingAssayFile meta = zircSubmissionService.getRequiredAssayFile(fileId);
        File onDisk = zircSubmissionService.resolveAttachmentPath(meta);
        MediaType type = meta.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(meta.getContentType());
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + meta.getOriginalFilename() + "\"")
                .body(new FileSystemResource(onDisk));
    }
}
