package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.zfin.publication.Publication;

import javax.persistence.*;


/**
 * High throughput meta dataset publications
 */
@Entity
@Table(name = "htp_dataset_publication")
@Setter
@Getter

public class HTPDatasetPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hdp_pk_id")
    private long ID;


    @ManyToOne
    @JoinColumn(name = "hdp_dataset_zdb_id")
    private HTPDataset htpDataset;

    @ManyToOne
    @JoinColumn(name = "hdp_pub_zdb_id")
    private Publication publication;

}
