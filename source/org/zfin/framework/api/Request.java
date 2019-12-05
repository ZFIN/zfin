package org.zfin.framework.api;


import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Setter
@Getter
public class Request {

    @JsonView(View.Default.class)
    String uri;

    @JsonView(View.Default.class)
    TreeMap<String, List<String>> parameterMap = new TreeMap<>();

    void setParameterMap(Map<String, String[]> parameterMap) {

        parameterMap.forEach((key, values) -> {
            this.parameterMap.put(key, Arrays.asList(values));
        });
    }

}
