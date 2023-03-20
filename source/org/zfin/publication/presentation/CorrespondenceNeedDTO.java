package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;
import org.zfin.publication.CorrespondenceNeedReason;

@Data
public class CorrespondenceNeedDTO {

    @JsonView(View.API.class)
    private long id;

    @JsonView(View.API.class)
    private String name;

    @JsonView(View.API.class)
    private boolean needed;

    public static CorrespondenceNeedDTO fromCorrespondenceNeedReason(CorrespondenceNeedReason reason, boolean needed) {
        CorrespondenceNeedDTO dto = new CorrespondenceNeedDTO();
        dto.setId(reason.getId());
        dto.setName(reason.getName());
        dto.setNeeded(needed);
        return dto;
    }
}
