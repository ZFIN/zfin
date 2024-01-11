package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

@Setter
@Getter
public class SequenceFeature {

    @JsonView({View.API.class, View.ExpressedGeneAPI.class, View.UI.class})
    public String zdbID;

    public String nameOrder;

    @JsonView({View.API.class, View.ExpressedGeneAPI.class})
    public String name;
}
