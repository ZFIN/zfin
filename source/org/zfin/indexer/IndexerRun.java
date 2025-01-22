package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.time.DateUtils;
import org.zfin.framework.api.Duration;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@Log4j2
@Setter
@Getter
@Entity
@Table(name = "ui.indexer_run")
public class IndexerRun extends BaseEntity {

    @Id
    @JsonView(View.API.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ir_id", nullable = false)
    private long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonView(View.API.class)
    @Column(name = "ir_start_date")
    private LocalDateTime startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @JsonView(View.API.class)
    @Column(name = "ir_end_date")
    private LocalDateTime endDate;

    @JsonView(View.API.class)
    @Column(name = "ir_duration")
    private Long duration;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "indexerRun", fetch = FetchType.LAZY)
    @OrderBy("startDate")
    private Set<IndexerInfo> indexerInfos;

    @JsonView(View.API.class)
    public String getDurationString() {
        if (startDate == null || endDate == null)
            return null;
        Duration duration = new Duration(startDate, endDate);
        return duration.toString();
    }

    @JsonView(View.API.class)
    public String getStartDay() {
        if (DateUtils.isSameDay(Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()), Date.from(Instant.now())))
            return "Today";
        if (DateUtils.isSameDay(Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()), Date.from(Instant.now().minus(1, ChronoUnit.DAYS))))
            return "Yesterday";
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("YYYY-MM-DD");
        return startDate.format(formatters);
    }

    @JsonView(View.API.class)
    public String getStartTime() {
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("hh:mm:dd");
        return startDate.format(formatters);
    }

    @JsonView(View.API.class)
    @JsonProperty("currentDuration")
    public String getCurrentDuration() {
        if (endDate != null)
            return null;
        Duration duration = new Duration(startDate, LocalDateTime.now());
        return duration.toString();
    }

    @Transient
    @JsonView(View.API.class)
    private Boolean isRunning;

}
