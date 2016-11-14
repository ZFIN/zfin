package org.zfin.orthology.presentation;

import java.util.Set;

/**
 * Convenience class for orthology presentation
 */
public class OrthologEvidenceGroupedByCode {
    private String code;
    private String name;
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
}
