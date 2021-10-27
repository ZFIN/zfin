
package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

@Setter
@Getter
public class FluorescentProteinMarker {

    @JsonView(View.API.class)
    private FluorescentMarker marker;

    @JsonView(View.API.class)
    public FluorescentProtein protein;

}
