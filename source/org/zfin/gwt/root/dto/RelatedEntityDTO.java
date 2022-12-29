package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.api.View;
import org.zfin.publication.Publication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class RelatedEntityDTO implements IsSerializable, HasLink, Comparable , Serializable {

    // data
    @JsonView(View.AntibodyAliasAPI.class)
    protected String zdbID ;  // the primary key of this object
    @JsonView(View.AntibodyAliasAPI.class)
    protected String dataZdbID; // the attached value
    @JsonView(View.AntibodyAliasAPI.class)
    protected String publicationZdbID;
    protected PublicationDTO publication;
    protected List<PublicationDTO> associatedPublications;

    // display
    @JsonView(View.API.class)
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

    public PublicationDTO getPublication() {
        return publication;
    }

    public void setPublication(PublicationDTO publication) {
        this.publication = publication;
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

    public List<PublicationDTO> getAssociatedPublications() {
        return associatedPublications;
    }

    public void setAssociatedPublications(List<PublicationDTO> associatedPubs) {
        associatedPublications = associatedPubs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RelatedEntityDTO that = (RelatedEntityDTO) o;

        if (editable != that.editable) return false;
        if (link != null ? !link.equals(that.link) : that.link != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (publicationZdbID != null ? !publicationZdbID.equals(that.publicationZdbID) : that.publicationZdbID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = publicationZdbID != null ? publicationZdbID.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (link != null ? link.hashCode() : 0);
        result = 31 * result + (editable ? 1 : 0);
        return result;
    }
}
