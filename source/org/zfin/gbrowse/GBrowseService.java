package org.zfin.gbrowse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.service.MarkerService;

import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;


public class GBrowseService {

    private final static Logger logger = LogManager.getLogger(GBrowseService.class);

    public static GBrowseTrack[] getGBrowseTracks(Marker marker) {
        GeneTrackDisplayMarker displayMarker = GeneTrackDisplayMarker.getInstance(marker.getType());
        if (displayMarker == null) {
            return null;
        }
        return displayMarker.getTracks();
    }

    public static Marker getGbrowseTrackingGene(Marker marker) {
        Marker relatedMarker = null;
        GeneTrackDisplayMarker displayMarker = GeneTrackDisplayMarker.getInstance(marker.getType());
        if (displayMarker == null || displayMarker.getRelationshipType() == null) {
            return marker;
        }
        MarkerRelationship.Type type = displayMarker.getRelationshipType();
        Set<RelatedMarker> relatedMarkerSet = MarkerService.getRelatedMarkers(marker, type);
        if (CollectionUtils.isNotEmpty(relatedMarkerSet)) {
            if (relatedMarkerSet.size() > 1 && marker.getType().equals(Marker.Type.EST)) {
                logger.error("More than one gene found that encodes for EST " + marker.getAbbreviation());
                logger.error("Set of related markers: " + relatedMarkerSet);
            }
            if (marker.getType().equals(Marker.Type.SNP)) {
                for (RelatedMarker relatedMarkerSnp : relatedMarkerSet) {
                    Marker otherMarker = relatedMarkerSnp.getMarker();
                    if (otherMarker.getType().equals(Marker.Type.GENE)) {
                        relatedMarker = otherMarker;
                    }
                }
            } else {
                relatedMarker = relatedMarkerSet.iterator().next().getMarker();
            }
        }
        return relatedMarker;
    }

    public static Marker getGbrowseTrackingGene(Feature feature) {
        List<Marker> relatedMarkerSet = getFeatureRepository().getMarkerIsAlleleOf(feature);
        Marker marker = relatedMarkerSet.get(0);
        if (getGbrowseTrackingGene(marker) != null) {
            return marker;
        }
        return null;
    }
}

