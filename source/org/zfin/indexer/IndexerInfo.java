package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;
import org.zfin.ontology.HumanGeneDetail;

import javax.persistence.*;
import java.time.LocalDateTime;

@Log4j2
@Setter
@Getter
@Entity
@Table(name = "ui.indexer_info")
public class IndexerInfo extends BaseEntity {

    @Id
    @JsonView(View.ExpressionPublicationUI.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ii_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ii_ir_id")
    private IndexerRun indexerRun;

    @Column(name = "ii_name")
    private String name;

    @Column(name = "ii_start_date")
    private LocalDateTime startDate;

    @Column(name = "ii_duration")
    private Long duration;

    @Column(name = "ii_count")
    private Integer count;


}
