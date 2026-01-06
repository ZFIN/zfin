package org.zfin.construct;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.StringEnumValueUserType;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 4/1/14
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Setter
@Getter
@Entity
@Table(name = "construct_component")
public class ConstructComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cc_pk_id")
    private int ID;

    @Column(name = "cc_construct_zdb_id")
    private String constructZdbID;

    @Column(name = "cc_component_type")
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@Parameter(name = "enumClassname", value = "org.zfin.construct.ConstructComponent$Type")})
    private Type type;

    @Column(name = "cc_component_zdb_id")
    private String componentZdbID;

    @Transient
    private String markerZDB;

    @Column(name = "cc_order")
    private int componentOrder;

    @Column(name = "cc_cassette_number")
    private int componentCassetteNum;

    @Column(name = "cc_component_category")
    private String componentCategory;

    @Column(name = "cc_component")
    private String componentValue;

    public enum Type {
        PROMOTER_OF("promoter of"),
        PROMOTER_OF_("promoter of "),
        CODING_SEQUENCE_OF("coding sequence of"),
        CODING_SEQUENCE_OF_("coding sequence of "),
        CONTROLLED_VOCAB_COMPONENT("controlled vocab component"),
        CONTROLLED_VOCAB_COMPONENT_("controlled vocab component "),
        TEXT_COMPONENT("text component"),
        TEXT_COMPONENT_("text component "),
        UNKNOWN_COMPONENT("unknown component"),
        CODING_COMPONENT("coding component"),
        PROMOTER_COMPONENT("promoter component"),
        CODING_SEQUENCE_COMPONENT("coding sequence component"),
        CASSETTE_DELIMITER("cassette delimiter"),
        CONSTRUCT_WRAPPER_COMPONENT("construct wrapper component");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }


        public static Type fromString(String text) {
            if (text != null) {
                for (Type b : Type.values()) {
                    if (text.equals(b.value)) {
                        return b;
                    }
                }
            }
            return UNKNOWN_COMPONENT; // Or null, based on how you want to handle unknown cases
        }
    }

    public Type getComponentCategoryEnum() {
        return Type.fromString(componentCategory);
    }

}
