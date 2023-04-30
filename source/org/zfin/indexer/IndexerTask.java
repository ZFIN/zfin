package org.zfin.indexer;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Log4j2
@Setter
@Getter
@Entity
@Table(name = "ui.indexer_task")
public class IndexerTask extends BaseEntity {

    @Id
    @JsonView(View.ExpressionPublicationUI.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "it_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "it_ii_id")
    private IndexerInfo indexerInfo;

    @Column(name = "it_name")
    private String name;

    @Column(name = "it_start_date")
    private LocalDateTime startDate;

    @Column(name = "it_duration")
    private Long duration;

    public enum Type {
        INPUT_OUTPUT("Input/Output"),
        DELETE("Delete"),
        SAVE("Save");

        private String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }
    }

}

