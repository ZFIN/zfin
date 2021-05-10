package org.zfin.mutant;

/**
 * Created with IntelliJ IDEA.
 * User: Prita
 * Date: 11/25/13
 * Time: 11:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConstructNameStaging {
    private long ID;
    private String cnsRelationshipName;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getCnsRelationshipName() {
        return cnsRelationshipName;
    }

    public void setCnsRelationshipName(String cnsRelationshipName) {
        this.cnsRelationshipName = cnsRelationshipName;
    }

    public String getCnsComponent() {
        return cnsComponent;
    }

    public void setCnsComponent(String cnsComponent) {
        this.cnsComponent = cnsComponent;
    }

    private String cnsComponent;

}
