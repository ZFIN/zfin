package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DiscriminatorFormula;
import org.zfin.infrastructure.EntityZdbID;

/**
 * linkage info for singleton records, i.e. old linkage members that were never paired up.
 */
@Entity
@Table(name = "linkage_single")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(lsingle_member_zdb_id) " +
        "WHEN 'ALT' THEN 'Feature' " +
        "ELSE 'Marker ' " +
        "END"
)
@Getter
@Setter
public class SingletonLinkage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lsingle_pk_id")
    private long id;

    @Column(name = "lsingle_member_zdb_id", insertable = false, updatable = false)
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lsingle_lnkg_zdb_id")
    private Linkage linkage;

    @Transient
    protected EntityZdbID entity;

    public EntityZdbID getEntity() {
        return entity;
    }
}
