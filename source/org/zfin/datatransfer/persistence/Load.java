package org.zfin.datatransfer.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A recurring data load, such as the NCBI Gene load. The files it reads are {@link LoadFile}s and each
 * run on a file is a {@link LoadFileLog}.
 */
@Entity
@Table(name = "load")
@Getter
@Setter
public class Load {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ld_id")
    private Long id;

    @Column(name = "ld_name")
    private String name;

    @Column(name = "ld_description")
    private String description;
}
