package org.zfin.zirc.dto;

import org.zfin.zirc.entity.GenotypingAssayFile;

import java.time.Instant;
import java.util.Date;

/**
 * One uploaded attachment on a {@link GenotypingAssayFile}. The
 * server-side {@code storedPath} is intentionally not exposed — clients
 * fetch the file via {@code GET /api/zirc/assays/attachments/{id}/content}
 * rather than hand-rolling a path.
 */
public record AssayFileDTO(
        Long id,
        String originalFilename,
        String contentType,
        Long fileSize,
        Instant uploadedAt) {

    public static AssayFileDTO of(GenotypingAssayFile f) {
        Date d = f.getUploadedAt();
        return new AssayFileDTO(
                f.getId(),
                f.getOriginalFilename(),
                f.getContentType(),
                f.getFileSize(),
                d == null ? null : d.toInstant());
    }
}
