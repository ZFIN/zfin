package org.zfin.marker.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class RelatedEntityDTO implements IsSerializable, HasLink, Comparable  {

    // data
    protected String dataZdbID;
    protected String publicationZdbID;

    // display
    protected String name ;
    protected String link;
    private boolean editable =true ;

    public RelatedEntityDTO(){}

    public RelatedEntityDTO(String dataZdbID, String name,String publicationZdbID){
        this.dataZdbID = dataZdbID;
        this.name = name ;
        if(publicationZdbID!=null && true==publicationZdbID.startsWith("ZDB-PUB-")){
            this.publicationZdbID = publicationZdbID;
        }
    }

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
        this.publicationZdbID = publicationZdbID;
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

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public RelatedEntityDTO create(String dataZdbID,String name,String publicationZdbID) {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
        relatedEntityDTO.setDataZdbID(dataZdbID);
        relatedEntityDTO.setPublicationZdbID(publicationZdbID);
        relatedEntityDTO.setName(name);
        return relatedEntityDTO;
    }


    public RelatedEntityDTO deepCopy() {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO() ;
        relatedEntityDTO.setDataZdbID(dataZdbID);
        relatedEntityDTO.setPublicationZdbID(publicationZdbID);
        relatedEntityDTO.setName(name);
        relatedEntityDTO.setLink(link);
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

    public int compareTo(Object o) {
        RelatedEntityDTO other = (RelatedEntityDTO)o;
        return getOrderingValue().compareTo(other.getOrderingValue());
    }
}
