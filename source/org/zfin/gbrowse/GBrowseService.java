package org.zfin.gbrowse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.feature.Feature;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.mapping.GbrowseTrack;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.RelatedMarker;
import org.zfin.marker.service.MarkerService;
import org.zfin.properties.ZfinPropertiesEnum;

import java.util.List;
import java.util.Set;

import static org.zfin.repository.RepositoryFactory.getFeatureRepository;


public class GBrowseService {

    private final static Logger logger = Logger.getLogger(GBrowseService.class);

    public static GBrowseImage buildTranscriptGBrowseImage(Marker gene, Transcript highlightedTranscript) {
        GBrowseImage image = new GBrowseImage();

        StringBuilder imageURL = new StringBuilder();
        imageURL.append("/");
        imageURL.append(ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT);
        imageURL.append("?grid=0");
        imageURL.append("&options=mRNA 0");
        imageURL.append("&type=mRNA");

        StringBuilder linkURL = new StringBuilder();
        linkURL.append("/");
        linkURL.append(ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT);

        imageURL.append("&name=");
        imageURL.append(gene.getZdbID());
        if (highlightedTranscript != null) {
            imageURL.append("&h_feat=");
            imageURL.append(highlightedTranscript.getAbbreviation());
        }

        linkURL.append("?name=");
        linkURL.append(gene.getZdbID());


        image.setImageURL(imageURL.toString());
        image.setLinkURL(linkURL.toString());

        return image;
    }


    public static GbrowseTrack[] getGBrowseTracks(Marker marker) {
        GeneTrackDisplayMarker displayMarker = GeneTrackDisplayMarker.getDisplayMarker(marker);
        if (displayMarker == null)
            return null;
        return displayMarker.getTracks();
    }

    public static Marker getGbrowseTrackingGene(Marker marker) {
        Marker relatedMarker = null;
        GeneTrackDisplayMarker displayMarker = GeneTrackDisplayMarker.getDisplayMarker(marker);
        if (displayMarker == null || displayMarker.getRelationshipType() == null)
            return marker;
        MarkerRelationship.Type type = displayMarker.getRelationshipType();
        if (type != null) {
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
        }
        return relatedMarker;
    }

    public static Marker getGbrowseTrackingGene(Feature feature) {
        List<Marker> relatedMarkerSet = getFeatureRepository().getMarkerIaAlleleOf(feature);
        Marker marker = relatedMarkerSet.get(0);
        if (getGbrowseTrackingGene(marker) != null)
            return marker;
        return null;
    }
}

enum GeneTrackDisplayMarker {
    GENE(Marker.Type.GENE, null, GbrowseTrack.GENES),
    GENEP(Marker.Type.GENEP, null, GbrowseTrack.GENES),
    EST(Marker.Type.EST, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT, GbrowseTrack.GENES),
    CDNA(Marker.Type.CDNA, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT, GbrowseTrack.GENES),
    RAPD(Marker.Type.RAPD, MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT, GbrowseTrack.GENES),
    BAC(Marker.Type.BAC, null, GbrowseTrack.GENES, GbrowseTrack.TRANSCRIPTS, GbrowseTrack.CLONE),
    PAC(Marker.Type.PAC, null, GbrowseTrack.GENES, GbrowseTrack.TRANSCRIPTS, GbrowseTrack.CLONE),
    FOSMID(Marker.Type.FOSMID, null, GbrowseTrack.GENES, GbrowseTrack.TRANSCRIPTS, GbrowseTrack.CLONE),
    SSLP(Marker.Type.SSLP, MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT, GbrowseTrack.GENES),
    SNP(Marker.Type.SNP, MarkerRelationship.Type.CONTAINS_POLYMORPHISM, GbrowseTrack.GENES),;

    private Marker.Type type;
    private MarkerRelationship.Type relationshipType;
    private GbrowseTrack[] track;

    GeneTrackDisplayMarker(Marker.Type type, MarkerRelationship.Type relationshipType, GbrowseTrack... track) {
        this.type = type;
        this.relationshipType = relationshipType;
        this.track = track;
    }

    public static GeneTrackDisplayMarker getDisplayMarker(Marker marker) {
        for (GeneTrackDisplayMarker displayMarker : values()) {
            if (displayMarker.getType().equals(marker.getType()))
                return displayMarker;
        }
        return null;
    }

    public Marker.Type getType() {
        return type;
    }

    public MarkerRelationship.Type getRelationshipType() {
        return relationshipType;
    }

    public GbrowseTrack[] getTracks() {
        return track;
    }
}
