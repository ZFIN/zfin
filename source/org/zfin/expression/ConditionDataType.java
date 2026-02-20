package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Getter
@Setter
@Entity
@Table(name = "condition_data_type")
public class ConditionDataType implements Comparable<ConditionDataType> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ConditionDataType")
    @GenericGenerator(name = "ConditionDataType",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "CDT")
            })
    @Column(name = "cdt_zdb_id")
    private String zdbID;
    @Column(name = "cdt_name")
    private String name;
    @Column(name = "cdt_group")
    private String group;
    @Column(name = "cdt_significance")
    private int significance;

    @Override
    public int compareTo(ConditionDataType o) {
        return significance - o.getSignificance();
    }
}
