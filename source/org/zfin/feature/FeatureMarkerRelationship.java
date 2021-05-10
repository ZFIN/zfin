package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "feature_marker_relationship")
public class FeatureMarkerRelationship implements Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FMREL"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fmrel_zdb_id")
    private String zdbID;
    @Column(name = "fmrel_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum")})
    private FeatureMarkerRelationshipTypeEnum type;
    @ManyToOne
    @JoinColumn(name = "fmrel_ftr_zdb_id")
    private Feature feature;
    @ManyToOne
    @JoinColumn(name = "fmrel_mrkr_zdb_id")
    private Marker marker;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "recattrib_data_zdb_id")
    private Set<PublicationAttribution> publications;
    @ManyToOne
    @JoinColumn(name = "fmrel_type", insertable=false, updatable=false)
    private FeatureMarkerRelationshipType featureMarkerRelationshipType;


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureMarkerRelationshipTypeEnum getType() {
        return type;
    }

    public void setType(FeatureMarkerRelationshipTypeEnum type) {
        this.type = type;
    }

    public FeatureMarkerRelationshipType getFeatureMarkerRelationshipType() {
        return featureMarkerRelationshipType;
    }

    public void setFeatureMarkerRelationshipType(FeatureMarkerRelationshipType featureMarkerRelationshipType) {
        this.featureMarkerRelationshipType = featureMarkerRelationshipType;
    }

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

    @Override
    public String toString() {
        return "FeatureMarkerRelationship{" +
                "zdbID='" + zdbID + '\'' +
                ", type='" + type + '\'' +
                ", feature='" + feature + '\'' +
                ", marker=" + marker +
//                ", feature=" + feature +
                '}';
    }

    public Set<PublicationAttribution> getPublications() {
        if (publications == null)
            return new HashSet<PublicationAttribution>();
        return publications;
    }

    public void setPublications(Set<PublicationAttribution> publications) {
        this.publications = publications;
    }

    public int getPublicationCount() {
        if (publications == null)
            return 0;
        else
            return publications.size();
    }

    public Publication getSinglePublication() {
        if (getPublicationCount() == 1) {
            for (PublicationAttribution pubAttr : getPublications())
                return pubAttr.getPublication();
        }
        return null;
    }
    public int compareTo(Object anotherMarkerRelationship) {
        return marker.compareTo(((FeatureMarkerRelationship) anotherMarkerRelationship).getMarker());
    }

    public boolean isMarkerIsGene() {

        if(marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)||marker.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)) {
            return true;
        }  else {
            return false;
        }
    }
}

