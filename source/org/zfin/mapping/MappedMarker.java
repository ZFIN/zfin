package org.zfin.mapping;

import org.zfin.people.Person;
import org.zfin.people.Lab;
import org.zfin.marker.Marker;

public class MappedMarker implements Comparable{
    private String zdbID;
    private String refcrossID;
    private Marker marker;
    private Person submitter;
    private Lab lab;
    private Person owner;
    private String comments;
    private String lg;
    private Float lgLocation;

    public int compareTo(Object o) {
        if (o == null){
           return -1 ;
        }
        else
        if (false==(o instanceof MappedMarker)){
           return o.toString().compareTo(toString()) ;
        }
        // both MappedMarker
        else{
            MappedMarker mappedMarker = (MappedMarker) o ;
            if(false==lg.equalsIgnoreCase(mappedMarker.getLg())){
                return lg.toLowerCase().compareTo(mappedMarker.getLg().toLowerCase());
            }
            else{
                return marker.compareTo(mappedMarker.getMarker());
            }
        }
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getRefcrossID() {
        return refcrossID;
    }

    public void setRefcrossID(String refcrossID) {
        this.refcrossID = refcrossID;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }

    public Float getLgLocation() {
        return lgLocation;
    }

    public void setLgLocation(Float lgLocation) {
        this.lgLocation = lgLocation;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

}
