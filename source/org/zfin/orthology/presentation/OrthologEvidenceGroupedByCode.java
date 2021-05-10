package org.zfin.orthology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

import java.util.Set;

/**
 * Convenience class for orthology presentation
 */
public class OrthologEvidenceGroupedByCode {

    @JsonView(View.OrthologyAPI.class)
    private String code;
    @JsonView(View.OrthologyAPI.class)
    private String name;
    @JsonView(View.OrthologyAPI.class)
    private Set<String> pubIds;

    public OrthologEvidenceGroupedByCode(String code, String name, Set<String> pubIds) {
        this.code = code;
        this.name = name;
        this.pubIds = pubIds;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Set<String> getPubIds() {
        return pubIds;
    }

    @JsonView(View.OrthologyAPI.class)
    public int getPublicationCount() {
        return pubIds.size();
    }


}
