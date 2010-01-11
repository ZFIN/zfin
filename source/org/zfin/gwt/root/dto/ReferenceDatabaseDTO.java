package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
@SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
public class ReferenceDatabaseDTO implements IsSerializable {

    private String zdbID;
    private String name;
    private String type;
    private String superType;
    private String blastName;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getNameAndType() {
        return name + " - " + type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSuperType() {
        return superType;
    }

    public void setSuperType(String superType) {
        this.superType = superType;
    }

    public String getBlastName() {
        return blastName;
    }

    public void setBlastName(String blastName) {
        this.blastName = blastName;
    }

    public ReferenceDatabaseDTO clone() {
        ReferenceDatabaseDTO refdbDTO = new ReferenceDatabaseDTO();
        refdbDTO.blastName = blastName;
        refdbDTO.name = name;
        refdbDTO.superType = superType;
        refdbDTO.type = type;
        refdbDTO.zdbID = zdbID;
        return refdbDTO;
    }


    public String toString() {
        String returnString = "";
        returnString += "zdbID: " + zdbID + "\n";
        returnString += "name: " + name + "\n";
        returnString += "type: " + type + "\n";
        returnString += "superType: " + superType + "\n";
        return returnString;
    }
}
