package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

/**
 */
@Getter
@Setter
public class GoEvidenceCode {

    // enum is it root.dto, atleast for now

    @JsonView(View.API.class)
    private String code;
    @JsonView(View.API.class)
    private String name;
    private Integer order;

}
