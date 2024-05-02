package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "FOREIGN_DB_DATA_TYPE")
public class ForeignDBDataType {

    public enum SuperType{
        INFERENCE("inference"),
        ONTOLOGY("ontology"),
        ORTHOLOG("ortholog"),
        PROTEIN("protein"),
        PUBLICATION("publication"),
        SEQUENCE("sequence"),
        SUMMARY_PAGE("summary page");

        private String value;

        SuperType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        public String toString(){
            return this.value ;
        }

        public static SuperType getType(String type) {
            for (SuperType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No reference supertype of string " + type + " found.");
        }
    }

    public enum DataType {
        ANATOMY("anatomy"),
        CELL_ONTOLOGY("cell ontology"),
        COMMON_ANATOMY_REFERENCE_ONTOLOGY("common anatomy reference ontology"),
        DOMAIN("domain"),
        GENE_ONTOLOGY("gene ontology"),
        GENOMIC("Genomic"),
        OTHER("other"),
        ORTHOLOG("ortholog"),
        POLYPEPTIDE("Polypeptide"),
        PUBLICATION("publication"),
        //VEGA_TRANSCRIPT("Vega Transcript"),
        RNA("RNA"),
        SEQUENCE_CLUSTERS("Sequence Clusters"),
        TELEOST_ANATOMY_ONTOLOGY("teleost anatomy ontology"),
        ;

        private String value;
        @JsonValue
        public String getValue() {
            return value;
        }


        DataType(String value) {
            this.value = value;
        }

        public String toString(){
            return this.value ;
        }

        public static DataType getType(String type) {
            for (DataType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No reference type of string " + type + " found.");
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdbdt_pk_id")
    private Long dataTypeID ;

    @JsonView(View.SequenceDetailAPI.class)
    @Column(name = "fdbdt_data_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.ForeignDBDataType$DataType")})
    private DataType dataType;
    @JsonView(View.SequenceDetailAPI.class)
    @Column(name = "fdbdt_super_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.ForeignDBDataType$SuperType")})
    private SuperType superType;
    @Column(name = "fdbdt_display_order")
    private int displayOrder;

    public String toString(){
        String returnString = "" ;
        returnString += getClass().getName() + " " ;
        returnString += dataTypeID + " " ;
        returnString += dataType + " " ;
        returnString += superType + " " ;
        returnString += displayOrder + " " ;
        return returnString ;
    }

}
