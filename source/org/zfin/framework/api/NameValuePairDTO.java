package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NameValuePairDTO {
    @JsonView(View.API.class)
    private String name;

    @JsonView(View.API.class)
    private String value;
}
