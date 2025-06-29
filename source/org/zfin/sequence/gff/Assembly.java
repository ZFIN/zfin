package org.zfin.sequence.gff;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.entity.BaseEntity;

@Setter
@Getter
@Entity
@Table(name = "assembly")
public class Assembly extends BaseEntity {

    @Id
    @Column(name = "a_pk_id", nullable = false)
    private long id;

    @Column(name = "a_name")
    private String name;

    @Column(name = "a_gcf_identifier")
    private String gcfIdentifier;
}

