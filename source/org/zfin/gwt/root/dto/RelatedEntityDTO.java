package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 */
public class RelatedEntityDTO implements IsSerializable, HasLink, Comparable , Serializable {

    // data
    protected String zdbID ;  // the primary key of this object
    protected String dataZdbID; // the attached value
    protected String publicationZdbID;

    // display
    protected String name ;
    protected String link;
    private boolean editable =true ;

    public RelatedEntityDTO(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        if(publicationZdbID==null || publicationZdbID.startsWith("ZDB-PUB-")){
            this.publicationZdbID = publicationZdbID;
        }
        else {
            throw new RuntimeException("publication is not a valid zdbID: "+publicationZdbID);
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public RelatedEntityDTO create(String dataZdbID,String name,String publicationZdbID) {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
        relatedEntityDTO.dataZdbID = dataZdbID;
        relatedEntityDTO.publicationZdbID = publicationZdbID;
        relatedEntityDTO.setName(name);
        return relatedEntityDTO;
    }


    public RelatedEntityDTO deepCopy() {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
        relatedEntityDTO.dataZdbID = dataZdbID;
        relatedEntityDTO.publicationZdbID = publicationZdbID;
        relatedEntityDTO.setName(name);
        relatedEntityDTO.link = link;
        return relatedEntityDTO;
    }

    public String getOrderingValue() {
        return name;
    }

    @Override
    public String toString() {
        return "RelatedEntityDTO{" +
                "name='" + name + '\'' +
                ", publicationZdbID='" + publicationZdbID + '\'' +
                ", link='" + link + '\'' +
                ", dataZdbID='" + dataZdbID + '\'' +
                ", editable=" + editable +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        RelatedEntityDTO other = (RelatedEntityDTO)o;
        return getOrderingValue().compareTo(other.getOrderingValue());
    }

}
