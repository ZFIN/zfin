package org.zfin.datatransfer.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * An upstream file or query that a {@link Load} reads, with the table it is parsed into. What stays the
 * same from run to run lives here; what differs per run lives in {@link LoadFileLog}.
 */
@Entity
@Table(name = "load_file")
@Getter
@Setter
public class LoadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lf_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lf_ld_id")
    private Load load;

    @Column(name = "lf_source")
    private String source;

    /** Schema-qualified table the file is parsed into, or null. */
    @Column(name = "lf_table_name")
    private String tableName;

    /** True if database dumps leave out the rows of {@link #tableName}; read by unload_production.sh. */
    @Column(name = "lf_exclude_from_dump")
    private boolean excludeFromDump;
}
