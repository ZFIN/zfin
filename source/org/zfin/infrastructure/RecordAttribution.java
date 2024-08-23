package org.zfin.infrastructure;

import org.hibernate.annotations.DiscriminatorFormula;

import jakarta.persistence.*;
import java.io.Serializable;


@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
        "CASE get_obj_type(recattrib_source_zdb_id)\n" +
                "                                    WHEN 'PUB' THEN\n" +
                "                                    CASE get_obj_type(recattrib_data_zdb_id)\n" +
                "                                         WHEN 'TERM' then 'Term   '\n" +
                "                                         ELSE             'Pub    '\n" +
                "                                    END\n" +
                "                                    WHEN 'PERS' THEN      'Person '\n" +
                "                                    WHEN 'COMPANY' THEN   'Company'\n" +
                "                                    WHEN 'JRNL' THEN      'Journal'\n" +
                "                                    WHEN 'LAB'  THEN      'Lab    '\n" +
                "                                    ELSE                  'other  '\n" +
                "                                 END")
@Table(name = "record_attribution")
@DiscriminatorValue("other  ")
public class RecordAttribution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recattrib_pk_id")
    private long id;
    @Column(name = "recattrib_data_zdb_id")
    protected String dataZdbID;
    @Column(name = "recattrib_source_zdb_id")
    private String sourceZdbID;
    @Column(name = "recattrib_source_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.infrastructure.RecordAttribution$SourceType")})
    private SourceType sourceType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public enum SourceType {
        ANATOMY_DEFINITION("anatomy definition"),
        CLONED_GENE("cloned gene"),
        FEATURE_TYPE("feature type"),
        MOLECULAR_CHARACTERIZATION("molecular characterization"),
        SEGREGATION("segregation"),
        STANDARD("standard"),
        TERM_DEFINITION("term definition"),
        FIRST_CURATED_SEQUENCE_PUB("first curated sequence pub"),
        SEQUENCE("sequence");

        private final String value;

        SourceType(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getSourceZdbID() {
        return sourceZdbID;
    }

    public void setSourceZdbID(String sourceZdbID) {
        this.sourceZdbID = sourceZdbID;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    //needs to override String methods for hash & equality

    public String getCompositeKey() {
        return dataZdbID + sourceZdbID + sourceType.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof RecordAttribution recordAttribution))
            return false;

        return recordAttribution.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

    public String toString() {
        String returnString = "";
        returnString += getDataZdbID();
        returnString += " ";
        returnString += getSourceZdbID();
        returnString += " ";
        returnString += getSourceType();

        return returnString;
    }

}
