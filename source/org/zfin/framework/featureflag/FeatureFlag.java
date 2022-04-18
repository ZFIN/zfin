package org.zfin.framework.featureflag;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.framework.api.View;

@JsonView(View.API.class)
@Data
public class FeatureFlag {
    private String name;
    private boolean enabled;
}
