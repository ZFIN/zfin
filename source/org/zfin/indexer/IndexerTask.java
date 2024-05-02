package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.Duration;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Log4j2
@Setter
@Getter
@Entity
@Table(name = "ui.indexer_task")
public class IndexerTask extends BaseEntity {

    @Id
    @JsonView(View.API.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "it_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "it_ii_id")
    private IndexerInfo indexerInfo;

    @JsonView(View.API.class)
    @Column(name = "it_name")
    private String name;

    @JsonView(View.API.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @Column(name = "it_start_date")
    private LocalDateTime startDate;

    @JsonView(View.API.class)
    @Column(name = "it_duration")
    private Long duration;

    @Transient
    private Boolean running;

    @JsonView(View.API.class)
    public String getDurationString() {
        if (startDate == null || duration == null)
            return null;
        long hours = duration / 3600;
        long minutes = (duration % 3600) / 60;
        long seconds = duration % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @JsonView(View.API.class)
    @JsonProperty("currentDuration")
    public String getCurrentDuration() {
        if (duration != null)
            return null;
        Duration duration = new Duration(startDate, LocalDateTime.now());
        return duration.toString();
    }

    public enum Type {
        INPUT_OUTPUT("Input/Output"),
        DELETE("Delete"),
        SAVE("Save");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }
    }

}

