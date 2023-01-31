package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

/**
 */
@Setter
@Getter
@SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
public class ReferenceDatabaseDTO implements IsSerializable {

    private String zdbID;
    private String name;
    private String originalDbName;
    private String type;
    private String superType;
    private String blastName;
    private String url;

    public String getNameAndType() {
        return name + " - " + type;
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
