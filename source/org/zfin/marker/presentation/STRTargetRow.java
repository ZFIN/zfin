package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;
import org.zfin.marker.Marker;
import org.zfin.mutant.SequenceTargetingReagent;

@Getter
public class STRTargetRow implements ZdbID {

    @JsonView(View.API.class)
    private Marker target;
    @JsonView(View.API.class)
    private SequenceTargetingReagent str;

    public STRTargetRow(SequenceTargetingReagent str, Marker target) {
        this.target = target;
        this.str = str;
    }

    @Override
    public String getZdbID() {
        return null;
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}
