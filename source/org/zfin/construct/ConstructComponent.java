package org.zfin.construct;

import lombok.Getter;
import lombok.Setter;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 4/1/14
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Setter
@Getter
public class ConstructComponent {


    private String constructZdbID;
    private Type type;

    private String componentZdbID;
    private String markerZDB;
    private int componentOrder;
    private int componentCassetteNum;


    private String componentCategory;
    private String componentValue;

    private int ID;

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
        CODING_COMPONENT("coding component");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }

}
