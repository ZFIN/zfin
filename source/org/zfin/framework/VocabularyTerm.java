package org.zfin.framework;

import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "vocabulary_term")
public class VocabularyTerm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vt_id", nullable = false)
    private long id;

    @Column(name = "vt_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vt_v_id")
    private Vocabulary vocabulary;

    @Column(name = "vt_date_created")
    private LocalDateTime startDate;


}
