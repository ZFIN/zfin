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
