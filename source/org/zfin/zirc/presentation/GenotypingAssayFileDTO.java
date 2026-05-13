package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.GenotypingAssayFile;

import java.util.Date;

/**
 * Wire format for one uploaded file on a {@link
 * org.zfin.zirc.entity.GenotypingAssay}. Inbound from the client is
 * only used for delete (id); creation is handled via multipart upload
 * to ZircDashboardController. The {@code storedPath} is never echoed
 * back — clients fetch the bytes via {@code /action/zirc/assay-file/
 * &#123;id&#125;/download}.
 */
@Getter
@Setter
@NoArgsConstructor
public class GenotypingAssayFileDTO {

    private Long id;
    private String kind;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Date uploadedAt;
    /** Display name of the uploader, or null. */
    private String uploadedBy;

    public static GenotypingAssayFileDTO from(GenotypingAssayFile f) {
        GenotypingAssayFileDTO dto = new GenotypingAssayFileDTO();
        dto.setId(f.getId());
        dto.setKind(f.getKind());
        dto.setOriginalFilename(f.getOriginalFilename());
        dto.setContentType(f.getContentType());
        dto.setFileSize(f.getFileSize());
        dto.setUploadedAt(f.getUploadedAt());
        if (f.getUploadedBy() != null) {
            dto.setUploadedBy(f.getUploadedBy().getFullName());
        }
        return dto;
    }
}
