package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 */
public class GoEvidenceCode {

    // enum is it root.dto, atleast for now

    @JsonView(View.API.class)
    private String code;
    @JsonView(View.API.class)
    private String name;
    private Integer order;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
