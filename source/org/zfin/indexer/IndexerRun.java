package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;

@Log4j2
@Setter
@Getter
@Entity
@Table(name = "ui.indexer_run")
public class IndexerRun extends BaseEntity {

    @Id
    @JsonView(View.ExpressionPublicationUI.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ir_id", nullable = false)
    private long id;

    @Column(name = "ir_start_date")
    private LocalDateTime startDate;

    @Column(name = "ir_end_date")
    private LocalDateTime endDate;

    @Column(name = "ir_duration")
    private Long duration;


}
