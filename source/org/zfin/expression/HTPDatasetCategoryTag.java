package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

/**
 * High throughput meta CV tags used to annotation datasets
 */
@Entity
@Table(name = "htp_dataset_category_tag")
@Setter
@Getter

public class HTPDatasetCategoryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hdct_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "hdct_dataset_zdb_id")
    private HTPDataset htpDataset;

    @Column(name = "hdct_category_tag")
    private String categoryTag;

}
