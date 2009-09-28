package org.zfin.marker.presentation.dto;

import java.util.List;

/**
 * This type mirrors the DBLink object.
 */
public class DBLinkDTO extends RelatedEntityDTO {

    protected ReferenceDatabaseDTO referenceDatabaseDTO = new ReferenceDatabaseDTO(); // will have to be defined to propagate
    protected Integer length ;
    protected String dataZdbID;
    protected String dataName ;
    protected String dbLinkZdbID;

    protected List<String> recordAttributions;

    public DBLinkDTO(){}

    public boolean isTranscriptDBLink(){
        return (dataZdbID != null && dataZdbID.startsWith("ZDB-TSCRIPT-")) ;
    }

    public DBLinkDTO(String view){
        String[] strings = view.split(":") ;
        referenceDatabaseDTO.setName(strings[0]);
        name = strings[1] ;
    }

    public String getView(){
        return referenceDatabaseDTO.getName() +":" + name ;
    }

    public void setView(String view){
        String[] strings = view.split(":") ;
        referenceDatabaseDTO.setName(strings[0]);
        name = strings[1] ;
    }

    public ReferenceDatabaseDTO getReferenceDatabaseDTO() {
        return referenceDatabaseDTO;
    }

    public void setReferenceDatabaseDTO(ReferenceDatabaseDTO referenceDatabaseDTO) {
        this.referenceDatabaseDTO = referenceDatabaseDTO;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getDbLinkZdbID() {
        return dbLinkZdbID;
    }

    public void setDbLinkZdbID(String dbLinkZdbID) {
        this.dbLinkZdbID = dbLinkZdbID;
    }

    public List<String> getRecordAttributions() {
        return recordAttributions;
    }

    public void setRecordAttributions(List<String> recordAttributions) {
        this.recordAttributions = recordAttributions;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }


    public DBLinkDTO deepCopy() {
        DBLinkDTO cloneDTO = new DBLinkDTO();

        cloneDTO.setDataZdbID(dataZdbID);
        cloneDTO.setDataName(dataName);
        cloneDTO.setDbLinkZdbID(dbLinkZdbID);
        cloneDTO.setLength(length);
        cloneDTO.setEditable(isEditable());
        cloneDTO.setLink(getLink());
        cloneDTO.setName(name);
        cloneDTO.setPublicationZdbID(publicationZdbID);
        cloneDTO.setRecordAttributions(getRecordAttributions());
        cloneDTO.setReferenceDatabaseDTO(getReferenceDatabaseDTO().clone());
        cloneDTO.setView(getView());

        return cloneDTO;
    }


    public String toString(){
        String returnString = "" ;
        returnString += "name: " + name + "\n";
        returnString += "dataZdbID: " + dataZdbID + "\n";
        returnString += "dataName: " + dataName+ "\n";
        returnString += "publicationZdbID: " + publicationZdbID + "\n";
        returnString += "dbLinkZdbID: " + dbLinkZdbID + "\n";
        returnString += "markerZdbID: " + dataZdbID + "\n";
        returnString += "length: " + length + "\n";
        returnString += "referenceDatabaseDTO: " + referenceDatabaseDTO+ "\n";
        returnString += "link: " + link+ "\n";
        return returnString ;
    }
}
