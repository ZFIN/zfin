package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * High throughput meta dataset object
 */
@Entity
@Table(name = "htp_dataset")
@Setter
@Getter

public class HTPDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfin")
    @GenericGenerator(name = "zfin",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "HTPDSET"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "hd_zdb_id")
    private String zdbID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatex_source_zdb_id")

    @Column(name = "hd_original_dataset_id")
    private String original_dataset_id;
    @Column(name = "hd_title")
    private String title;
    @Column(name = "hd_summary")
    private String summary;
    @Column(name = "hd_date_curated")
    private GregorianCalendar dateCurated;

}
