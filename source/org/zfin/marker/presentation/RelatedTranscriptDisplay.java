package org.zfin.marker.presentation;

import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.service.TranscriptService;

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

    public TreeSet<RelatedMarker> getNonWithdrawnTranscripts() {
         if (transcripts == null || transcripts.size() == 0) {
            return null;
         }
         TreeSet<RelatedMarker> nonWithdrawnTranscripts = new TreeSet<RelatedMarker> ();
         for (RelatedMarker marker : transcripts) {
             Transcript transcript = TranscriptService.convertMarkerToTranscript(marker.getMarker());
             if (!transcript.isWithdrawn()) {
                  nonWithdrawnTranscripts.add(marker);
             }
         }
         return nonWithdrawnTranscripts;
    }

    public TreeSet<RelatedMarker> getWithdrawnTranscripts() {
         if (transcripts == null || transcripts.size() == 0) {
            return null;
         }
         TreeSet<RelatedMarker> withdrawnTranscripts = new TreeSet<RelatedMarker> ();
         for (RelatedMarker marker : transcripts) {
             Transcript transcript = TranscriptService.convertMarkerToTranscript(marker.getMarker());
             if (transcript.isWithdrawn()) {
                  withdrawnTranscripts.add(marker);
             }
         }
         return withdrawnTranscripts;
    }

    public List<RelatedMarker> getWithdrawnList() {
         TreeSet<RelatedMarker> withdrawnTranscripts = getWithdrawnTranscripts();
         if (withdrawnTranscripts == null) {
            return null;
         }
         List<RelatedMarker> withdrawnlist = new ArrayList<RelatedMarker>(withdrawnTranscripts.size());
         withdrawnlist.addAll(withdrawnTranscripts);
         return withdrawnlist;
    }

    public List<RelatedMarker> getNonWithdrawnList() {
         TreeSet<RelatedMarker> nonWithdrawnTranscripts = getNonWithdrawnTranscripts();
         if (nonWithdrawnTranscripts == null) {
            return null;
         }
         List<RelatedMarker> nonWithdrawnlist = new ArrayList<RelatedMarker>(nonWithdrawnTranscripts.size());
         nonWithdrawnlist.addAll(nonWithdrawnTranscripts);
         return nonWithdrawnlist;
    }
}
