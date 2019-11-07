package org.zfin.framework.api;


import com.fasterxml.jackson.annotation.JsonView;

import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request {

    @JsonView(View.Default.class)
    String uri;

    @JsonView(View.Default.class)
    TreeMap<String, String[]> parameterMap = new TreeMap<>();

    void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap.putAll(parameterMap);
    }

}
