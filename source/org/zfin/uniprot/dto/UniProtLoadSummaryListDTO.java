package org.zfin.uniprot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class UniProtLoadSummaryListDTO {

    @JsonIgnore
    Map<String, UniProtLoadSummaryItemDTO> summaryList = new LinkedHashMap<>();

    @JsonValue
    public Collection<UniProtLoadSummaryItemDTO> values() {
        return summaryList.values();
    }

    public void putBeforeSummary(UniProtLoadSummaryItemDTO summary) {
        if (summaryList.containsKey(summary.description())) {
            UniProtLoadSummaryItemDTO existingSummary = summaryList.get(summary.description());
            summaryList.put(summary.description(), new UniProtLoadSummaryItemDTO(summary.description(), summary.beforeLoadCount(), existingSummary.afterLoadCount()));
        } else {
            summaryList.put(summary.description(), summary);
        }
    }

    public void putAfterSummary(UniProtLoadSummaryItemDTO summary) {
        if (summaryList.containsKey(summary.description())) {
            UniProtLoadSummaryItemDTO existingSummary = summaryList.get(summary.description());
            summaryList.put(summary.description(), new UniProtLoadSummaryItemDTO(summary.description(), existingSummary.beforeLoadCount(), summary.afterLoadCount()));
        } else {
            summaryList.put(summary.description(), summary);
        }
    }

}
