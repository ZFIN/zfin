package org.zfin.construct;

import org.zfin.marker.MarkerType;
import org.zfin.profile.Person;

import java.util.Date;
import java.util.Set;

public class ConstructCuration {

    private String zdbID;
    private String publicComments;
    private Person owner;
    private Set<ConstructRelationship> constructRelations;

    public Set<ConstructRelationship> getConstructRelations() {
        return constructRelations;
    }

    public void setConstructRelations(Set<ConstructRelationship> constructRelations) {
        this.constructRelations = constructRelations;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getPublicComments() {
        return publicComments;
    }

    public void setPublicComments(String publicComments) {
        this.publicComments = publicComments;
    }

    private String name;
    private MarkerType constructType;

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public MarkerType getConstructType() {
        return constructType;
    }

    public void setConstructType(MarkerType constructType) {
        this.constructType = constructType;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    private Date modDate;
    private Date createdDate;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
