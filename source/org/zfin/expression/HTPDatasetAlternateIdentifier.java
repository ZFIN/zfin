package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;


/**
 * High throughput meta dataset alternate identifier (a cross between db_link and zdb_replaced_data) object
 */
@Entity
@Table(name = "htp_dataset_alternate_identifier")
@Setter
@Getter

public class HTPDatasetAlternateIdentifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hdai_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "hdai_hd_zdb_id")
    private HTPDataset htpDataset;

    @Column(name = "hdai_accession_number")
    private String accessionNumber;

}
