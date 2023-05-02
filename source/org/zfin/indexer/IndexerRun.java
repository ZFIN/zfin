package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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


}
