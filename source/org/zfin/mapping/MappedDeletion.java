package org.zfin.mapping;

import org.zfin.marker.Marker;
import org.zfin.people.Person;

/**
 * Created by IntelliJ IDEA.
 * User: Peiran Song
 * Date: Aug 14, 2007
 * Time: 11:45:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class MappedDeletion {

    private String zdbID;
    private Marker marker;
    private String allele;
    private boolean present;

    public String getAllele() {
        return allele;
    }

    public void setAllele(String allele) {
        this.allele = allele;
    }

    private Person submitter;
    private Person owner;
    private String lg;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }


    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }
}
