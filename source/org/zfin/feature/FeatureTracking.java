package org.zfin.feature;

import org.zfin.feature.FeatureAlias;
import org.zfin.feature.FeatureAssay;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.feature.FeaturePrefix;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.EntityNotes;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.MappedDeletion;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.people.FeatureSource;
import org.zfin.people.FeatureSupplier;
import org.zfin.sequence.FeatureDBLink;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**ture
 */
public class FeatureTracking {

    private int pkid;

    public int getPkid() {
        return pkid;
    }

    public void setPkid(int pkid) {
        this.pkid = pkid;
    }

    public String getFeatTrackingFeatZdbID() {
        return featTrackingFeatZdbID;
    }

    public void setFeatTrackingFeatZdbID(String featTrackingFeatZdbID) {
        this.featTrackingFeatZdbID = featTrackingFeatZdbID;
    }

    public String getFeatTrackingFeatAbbrev() {
        return featTrackingFeatAbbrev;
    }

    public void setFeatTrackingFeatAbbrev(String featTrackingFeatAbbrev) {
        this.featTrackingFeatAbbrev = featTrackingFeatAbbrev;
    }

    private String featTrackingFeatZdbID;
    private String featTrackingFeatAbbrev;



}
