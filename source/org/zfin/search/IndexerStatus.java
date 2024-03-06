package org.zfin.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndexerStatus {

    private String status;

    private StatusMessages statusMessages;

    @Getter
    public static class StatusMessages {

        @JsonProperty("Time Elapsed")
        private String timeElapsed;

        @JsonProperty("Time taken")
        private String timeTaken;

    }

    public enum Status {
        BUSY, IDLE
    }
}
