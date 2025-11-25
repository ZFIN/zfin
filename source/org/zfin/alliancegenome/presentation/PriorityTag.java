package org.zfin.alliancegenome.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriorityTag {

    @JsonProperty("current_priority_tag")
    private CurrentPriorityTag currentPriorityTag;

/*
    @JsonProperty("all_priority_tags")
    private Map<String, String> allPriorityTags;
*/

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentPriorityTag {

        @JsonProperty("indexing_priority_id")
        private Long indexingPriorityId;

        @JsonProperty("indexing_priority")
        private String indexingPriority;

        @JsonProperty("confidence_score")
        private Double confidenceScore;

        @JsonProperty("validation_by_biocurator")
        private String validationByBiocurator;

        @JsonProperty("date_updated")
        private LocalDateTime dateUpdated;

        @JsonProperty("source_id")
        private Long sourceId;

        @JsonProperty("reference_curie")
        private String referenceCurie;

        @JsonProperty("mod_abbreviation")
        private String modAbbreviation;

        @JsonProperty("updated_by_email")
        private String updatedByEmail;

        @JsonProperty("updated_by_name")
        private String updatedByName;

        @JsonProperty("updated_by")
        private String updatedBy;

        @JsonProperty("indexing_priority_name")
        private String indexingPriorityName;

        public String getPriority() {
            return indexingPriority.substring(indexingPriority.length() - 1);

        }
    }
}