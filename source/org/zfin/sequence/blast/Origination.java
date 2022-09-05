package org.zfin.sequence.blast;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "blast_database_origination_type")
public class Origination {

    @Id
    @Column(name = "bdot_pk_id")
    private Long id;
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.blast.Origination$Type")})
    @Column(name = "bdot_type")
    private Type type;
    @Column(name = "bdot_definition")
    private String definition;

    public enum Type {
        CURATED,
        CURATED_IGNORE,
        GENERATED,
        LOADED,
        EXTERNAL,
        MARKERSEQUENCE,
        ;
    }
}
