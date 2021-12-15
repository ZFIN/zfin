package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;

@Getter
public class STRTargetRow {

    @JsonView(View.API.class)
    private Marker target;
    @JsonView(View.API.class)
    private SequenceTargetingReagent str;

    public STRTargetRow(SequenceTargetingReagent str, Marker target) {
        this.target = target;
        this.str = str;
    }

}
