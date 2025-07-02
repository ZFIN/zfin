package org.zfin.sequence.gff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.zfin.framework.entity.BaseEntity;
import org.zfin.marker.Marker;

import java.util.List;

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

    @Column(name = "a_order")
    @SortNatural
    private int order;

    @ManyToMany(mappedBy = "assemblies", fetch = FetchType.LAZY)
    private List<Marker> marker;
}

