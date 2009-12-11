package org.zfin.mutant;

import org.zfin.mutant.FeatureAlias;
import org.zfin.people.FeatureSource;
import org.zfin.people.MarkerSupplier;
import org.zfin.infrastructure.DataNote;
import org.zfin.mapping.MappedMarker;
import org.zfin.mapping.MappedDeletion;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 */
public class Feature {

    private String zdbID;
    private String name;
    private String abbreviation;
    private String abbreviationOrder;
    private String comments;
    private Zygosity maleZygosity;
    private Zygosity femaleZygosity;
    private Set<MappedDeletion> mappedDeletions;
    private Date dateEntered;
    private Set<FeatureMarkerRelationship> featureMarkerRelations;

    public FeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(FeatureType featureType) {
        this.featureType = featureType;
    }

    private Set<FeatureAlias> aliases;
    private FeatureType featureType;


    public Set<MappedDeletion> getMappedDeletions() {
        return mappedDeletions;
    }

    public void setMappedDeletions(Set<MappedDeletion> mappedDeletions) {
        this.mappedDeletions = mappedDeletions;
    }

    public Type getType() {
        return type;

    }

    public void setType(Type type) {
        this.type = type;
    }

    private Set<FeatureSource> sources;

    public Set<DataNote> getDataNote() {
        return dataNote;
    }

    public void setDataNote(Set<DataNote> dataNote) {
        this.dataNote = dataNote;
    }

    private Set<DataNote> dataNote;
    private Type type;


    public enum Type{
        DEFICIENCY("DEFICIENCY"),
        POINT_MUTATION("POINT_MUTATION"),
        TRANSLOC("TRANSLOC"),
        INSERTION("INSERTION"),
        SEQUENCE_VARIANT("SEQUENCE_VARIANT"),
        UNSPECIFIED("UNSPECIFIED"),
        COMPLEX_SUBSTITUTION("COMPLEX_SUBSTITUTION"),
        TRANSGENIC_INSERTION("TRANSGENIC_INSERTION"),
        INVERSION("INVERSION"),
        DELETION("DELETION");


        private String value;

        Type(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }
    }

    public Set<FeatureSource> getSources() {
        return sources;
    }

    public void setSources(Set<FeatureSource> sources) {
        this.sources = sources;
    }


    public FeatureAssay getFeatureAssay() {
        return featureAssay;
    }

    public void setFeatureAssay(FeatureAssay featureAssay) {
        this.featureAssay = featureAssay;
    }

    private  FeatureAssay featureAssay;



    public Set<FeatureAlias> getAliases() {
        return aliases;
    }

    public void setAliases(Set<FeatureAlias> aliases) {
        this.aliases = aliases;
    }

    public String getZdbID() {

        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAbbreviationOrder() {
        return abbreviationOrder;
    }

    public void setAbbreviationOrder(String abbreviationOrder) {
        this.abbreviationOrder = abbreviationOrder;
    }


    public Set<FeatureMarkerRelationship> getFeatureMarkerRelations() {
        return featureMarkerRelations;
    }

    public void setFeatureMarkerRelations(Set<FeatureMarkerRelationship> featureMarkerRelations) {
        this.featureMarkerRelations = featureMarkerRelations;
    }
    public int compareTo(Object otherFeature) {
        return getAbbreviationOrder().compareTo(((Feature) otherFeature).getAbbreviationOrder());
    }
}
