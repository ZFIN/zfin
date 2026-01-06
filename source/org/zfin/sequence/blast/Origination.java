package org.zfin.sequence.blast;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.StringEnumValueUserType;

@Setter
@Getter
@Entity
@Table(name = "blast_database_origination_type")
public class Origination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bdot_pk_id", nullable = false)
    private Long id;

    @Column(name = "bdot_type")
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.blast.Origination$Type")})
    private Type type;

    @Column(name = "bdot_definition")
    private String definition;

    public static enum Type {
        CURATED,
        CURATED_IGNORE,
        GENERATED,
        LOADED,
        EXTERNAL,
        MARKERSEQUENCE,
        ;
    }
}
