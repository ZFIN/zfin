package org.zfin.sequence.gff;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;
import org.zfin.marker.Marker;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "assembly")
public class Assembly extends BaseEntity {

    @Id
    @Column(name = "a_pk_id", nullable = false)
    private long id;

    @JsonView(View.SequenceAPI.class)
    @Column(name = "a_name")
    private String name;

    @Column(name = "a_gcf_identifier")
    private String gcfIdentifier;

    @Column(name = "a_order")
    @SortNatural
    private int order;

    @ManyToMany(mappedBy = "assemblies", fetch = FetchType.LAZY)
    private List<Marker> marker;

    // Assembly lives in a Set<Assembly> (Marker.assemblies, the owning side of marker_assembly),
    // so it needs value equality on the primary key. With inherited identity equality, the same
    // assembly row reached through two different paths is two unequal instances, the Set holds it
    // twice, and Hibernate emits a second marker_assembly insert that trips
    // marker_assembly_ma_a_pk_id_ma_mrkr_zdb_id_key. Compare on a_pk_id instead.
    //
    // instanceof rather than getClass() so a lazy Hibernate proxy compares equal to its target,
    // and the getter rather than the field so reading a proxy's id initializes it first.
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Assembly assembly))
            return false;
        return id == assembly.getId();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}

