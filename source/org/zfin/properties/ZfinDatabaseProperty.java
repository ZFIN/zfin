package org.zfin.properties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zdb_property")
@Getter
@Setter
public class ZfinDatabaseProperty {

    public static enum KeyName {
        RELEASE_NUMBER,
        RELEASE_COMMIT_HASH,
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zprop_id")
    private Long id;

    @Column(name = "zprop_name")
    private String name;

    @Column(name = "zprop_value")
    private String value;

    @Column(name = "zprop_type")
    private String type;

}
