package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * High throughput meta CV tag table
 */
@Entity
@Table(name = "htp_category_tag")
@Setter
@Getter

public class HTPCategoryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfin")
    @GenericGenerator(name = "zfin",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "HTPTAG"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "hct_zdb_id")
    private String zdbID;

    @Column(name = "hct_category_tag")
    private String categoryTag;


}
