package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;
import org.zfin.publication.CorrespondenceResolutionType;

@Data
public class CorrespondenceResolutionDTO {

    @JsonView(View.API.class)
    private long id;

    @JsonView(View.API.class)
    private String name;

    @JsonView(View.API.class)
    private boolean resolved;

    public static CorrespondenceResolutionDTO fromCorrespondenceResolutionType(CorrespondenceResolutionType resolutionType, boolean resolved) {
        CorrespondenceResolutionDTO dto = new CorrespondenceResolutionDTO();
        dto.setId(resolutionType.getId());
        dto.setName(resolutionType.getName());
        dto.setResolved(resolved);
        return dto;
    }
}
