package org.zfin.construct;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 4/1/14
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstructComponent {


    private String constructZdbID;
    private Type type;

    private String componentZdbID;
    private String markerZDB;
    private int componentOrder;
    private int componentCassetteNum;


    private String componentCategory;
    private String componentValue;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    private int ID;

    public String getComponentCategory() {
        return componentCategory;
    }

    public void setComponentCategory(String componentCategory) {
        this.componentCategory = componentCategory;
    }

    public enum Type {
        PROMOTER_OF("promoter of"),
        PROMOTER_OF_("promoter of "),
        CODING_SEQUENCE_OF("coding sequence of"),
        CODING_SEQUENCE_OF_("coding sequence of "),
        CCODING_SEQUENCE_OF("coding sequence of"),
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getConstructZdbID() {
        return constructZdbID;
    }

    public void setConstructZdbID(String constructZdbID) {
        this.constructZdbID = constructZdbID;
    }

    public String getComponentZdbID() {
        return componentZdbID;
    }

    public void setComponentZdbID(String componentZdbID) {
        this.componentZdbID = componentZdbID;
    }

    public String getMarkerZDB() {
        return markerZDB;
    }

    public void setMarkerZDB(String markerZDB) {
        this.markerZDB = markerZDB;
    }

    public int getComponentOrder() {
        return componentOrder;
    }

    public void setComponentOrder(int componentOrder) {
        this.componentOrder = componentOrder;
    }

    public int getComponentCassetteNum() {
        return componentCassetteNum;
    }

    public void setComponentCassetteNum(int componentCassetteNum) {
        this.componentCassetteNum = componentCassetteNum;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public void setComponentValue(String componentValue) {
        this.componentValue = componentValue;
    }
}
