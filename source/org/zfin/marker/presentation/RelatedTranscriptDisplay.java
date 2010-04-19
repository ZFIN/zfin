package org.zfin.marker.presentation;

import org.zfin.marker.Transcript;
import org.zfin.marker.Marker;
import org.zfin.gbrowse.presentation.GBrowseImage;

import java.util.*;

public class RelatedTranscriptDisplay  {

    private TreeSet<RelatedMarker> transcripts;
    private Marker gene;
    private List<GBrowseImage> gbrowseImages;


    public RelatedTranscriptDisplay() {
        transcripts = new TreeSet<RelatedMarker>(new RelatedTranscriptNameSort());
    }

    public TreeSet<RelatedMarker> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(TreeSet<RelatedMarker> transcripts) {
        this.transcripts = transcripts;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public void add(RelatedMarker rm) {
        transcripts.add(rm);
    }
    
    public List<GBrowseImage> getGbrowseImages() {
        return gbrowseImages;
    }

    public void setGbrowseImages(List<GBrowseImage> gbrowseImages) {
        this.gbrowseImages = gbrowseImages;
    }

    public static class RelatedTranscriptNameSort implements Comparator<RelatedMarker> {

        public int compare(RelatedMarker rmA, RelatedMarker rmB) {

            Transcript tA = (Transcript) rmA.getMarker();
            Transcript tB = (Transcript) rmB.getMarker();

            int typeCompare = tB.getTranscriptType().toString().compareTo(tA.getTranscriptType().toString());
            //if the types aren't equal, sort on that
            if (typeCompare != 0) return typeCompare;

            //if they're the same type, sort by abbreviationOrder - essentially by name
            return tA.getAbbreviationOrder().compareTo(tB.getAbbreviationOrder());

        }
    }

    /* This method is made to make CreateAlternatingTR happy, since it wants a list,
     * it's likely that this method is absurdly wasteful */
    public List<RelatedMarker> getList() {
        List<RelatedMarker> list = new ArrayList<RelatedMarker>(transcripts.size());
        list.addAll(transcripts);
        return list;
    }

}
