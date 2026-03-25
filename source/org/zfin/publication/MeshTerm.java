package org.zfin.publication;

import org.zfin.framework.StringEnumValueUserType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mesh_term")
public class MeshTerm implements Comparable<MeshTerm> {

    @Id
    @Column(name = "mesht_mesh_id", nullable = false)
    private String id;

    @Column(name = "mesht_term_name", nullable = false)
    private String name;

    @Column(name = "mesht_type", nullable = false)
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.publication.MeshTerm$Type")})
    private Type type;

    @Override
    public int compareTo(MeshTerm o) {
        return this.getName().compareTo(o.getName());
    }

    public enum Type {
        DESCRIPTOR, QUALIFIER, SUPPLEMENTARY
    }
}
