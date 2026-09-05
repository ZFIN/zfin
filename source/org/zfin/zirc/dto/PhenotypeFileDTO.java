package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.PhenotypeFile;

import java.time.Instant;
import java.util.Date;

/**
 * One uploaded image on a {@link PhenotypeFile} (ZFIN-10449). Mirrors
 * {@link AssayFileDTO}: the server-side {@code storedPath} is intentionally
 * not exposed — clients fetch the bytes via
 * {@code GET /api/zirc/phenotypes/attachments/{id}/content} rather than
 * hand-rolling a path.
 *
 * <p>Structurally identical to AssayFileDTO, and kept separate anyway: they
 * are payloads of different endpoints, and collapsing them would tie the two
 * owners' wire formats together for no gain. The attachments renderer accepts
 * either through a structural type.
 */
public record PhenotypeFileDTO(
        @NotNull Long id,
        @NotNull String originalFilename,
        String contentType,
        Long fileSize,
        Instant uploadedAt) {

    public static PhenotypeFileDTO of(PhenotypeFile f) {
        Date d = f.getUploadedAt();
        return new PhenotypeFileDTO(
                f.getId(),
                f.getOriginalFilename(),
                f.getContentType(),
                f.getFileSize(),
                d == null ? null : d.toInstant());
    }
}
