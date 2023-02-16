
package org.zfin.infrastructure;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "zdb_replaced_source")
public class ReplacedDataReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zrepls_pk_id")
    private long ID;

    @Column(name = "zrepls_comment")
    private String comment;

    @Column(name = "zrepls_old_zdb_id")
    // Always just an old ID not being found in the database
    private String oldZdbID;

    @Column(name = "zrepls_src_zdb_id")
    private String publicationID;

}
