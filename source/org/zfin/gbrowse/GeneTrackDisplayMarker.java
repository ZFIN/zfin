package org.zfin.gbrowse;

import org.zfin.genomebrowser.GenomeBrowserTrack;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class GeneTrackDisplayMarker {

    private static final Map<Marker.Type, GeneTrackDisplayMarker> instanceMap;
    static {
        Map<Marker.Type, GeneTrackDisplayMarker> tempMap = new HashMap<>();
        tempMap.put(Marker.Type.GENE, new GeneTrackDisplayMarker(null, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.GENEP, new GeneTrackDisplayMarker(null, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.EST, new GeneTrackDisplayMarker(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.CDNA, new GeneTrackDisplayMarker(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.RAPD, new GeneTrackDisplayMarker(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.BAC, new GeneTrackDisplayMarker(null, GenomeBrowserTrack.GENES, GenomeBrowserTrack.TRANSCRIPTS, GenomeBrowserTrack.CLONE));
        tempMap.put(Marker.Type.PAC, new GeneTrackDisplayMarker(null, GenomeBrowserTrack.GENES, GenomeBrowserTrack.TRANSCRIPTS, GenomeBrowserTrack.CLONE));
        tempMap.put(Marker.Type.FOSMID, new GeneTrackDisplayMarker(null, GenomeBrowserTrack.GENES, GenomeBrowserTrack.TRANSCRIPTS, GenomeBrowserTrack.CLONE));
        tempMap.put(Marker.Type.SSLP, new GeneTrackDisplayMarker(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.SNP, new GeneTrackDisplayMarker(MarkerRelationship.Type.CONTAINS_POLYMORPHISM, GenomeBrowserTrack.GENES));
        tempMap.put(Marker.Type.MRPHLNO, new GeneTrackDisplayMarker(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE, GenomeBrowserTrack.GENES, GenomeBrowserTrack.KNOCKDOWN_REAGENT, GenomeBrowserTrack.TRANSCRIPTS));
        tempMap.put(Marker.Type.CRISPR, new GeneTrackDisplayMarker(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE, GenomeBrowserTrack.GENES, GenomeBrowserTrack.KNOCKDOWN_REAGENT, GenomeBrowserTrack.TRANSCRIPTS));
        tempMap.put(Marker.Type.TALEN, new GeneTrackDisplayMarker(MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE, GenomeBrowserTrack.GENES, GenomeBrowserTrack.KNOCKDOWN_REAGENT, GenomeBrowserTrack.TRANSCRIPTS));
        tempMap.put(Marker.Type.TSCRIPT, new GeneTrackDisplayMarker(MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE, GenomeBrowserTrack.GENES, GenomeBrowserTrack.TRANSCRIPTS));
        instanceMap = Collections.unmodifiableMap(tempMap);
    }

    private MarkerRelationship.Type relationshipType;
    private GenomeBrowserTrack[] track;

    private GeneTrackDisplayMarker(MarkerRelationship.Type relationshipType, GenomeBrowserTrack... track) {
        this.relationshipType = relationshipType;
        this.track = track;
    }

    public static GeneTrackDisplayMarker getInstance(Marker.Type markerType) {
        return instanceMap.get(markerType);
    }

    public MarkerRelationship.Type getRelationshipType() {
        return relationshipType;
    }

    public GenomeBrowserTrack[] getTracks() {
        return track;
    }
}
