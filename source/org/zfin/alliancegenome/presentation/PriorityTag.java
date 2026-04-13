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

        @JsonProperty("predicted_indexing_priority")
        private String predictedIndexingPriority;

        @JsonProperty("predicted_indexing_priority_name")
        private String predictedIndexingPriorityName;

        @JsonProperty("curator_indexing_priority")
        private String curatorIndexingPriority;

        @JsonProperty("curator_indexing_priority_name")
        private String curatorIndexingPriorityName;

        @JsonProperty("confidence_score")
        private Double confidenceScore;

        @JsonProperty("date_updated")
        private LocalDateTime dateUpdated;

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

        public String getIndexingPriority() {
            return curatorIndexingPriority != null ? curatorIndexingPriority : predictedIndexingPriority;
        }

        public String getPriority() {
            String priority = getIndexingPriority();
            return priority.substring(priority.length() - 1);
        }
    }
}