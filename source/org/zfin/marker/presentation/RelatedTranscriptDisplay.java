package org.zfin.marker.presentation;

import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;

public class RelatedTranscriptDisplay  {

    private List<RelatedMarker> transcripts;
    private List<RelatedMarker> nonWithdrawnTranscripts;
    private List<RelatedMarker> withdrawnTranscripts;
    private Marker gene;
    private GBrowseImage gbrowseImage;

    public RelatedTranscriptDisplay() {
        transcripts = new ArrayList<>();
    }

    public List<RelatedMarker> getTranscripts() {
        List<RelatedMarker> transcriptsList = new ArrayList<>(transcripts);
        return transcriptsList;
    }

    public void setTranscripts(List<RelatedMarker> transcripts) {
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

    public GBrowseImage getGbrowseImage() {
        return gbrowseImage;
    }

    public void setGbrowseImage(GBrowseImage gbrowseImage) {
        this.gbrowseImage = gbrowseImage;
    }

    /* This method is made to make CreateAlternatingTR happy, since it wants a list,
     * it's likely that this method is absurdly wasteful */
    public List<RelatedMarker> getList() {
        List<RelatedMarker> list = new ArrayList<>(transcripts.size());
        list.addAll(transcripts);
        return list;
    }

    public List<RelatedMarker> getNonWithdrawnTranscripts() {
        if (transcripts == null || transcripts.size() == 0) {
            return null;
        }

        return TranscriptService.getSortedTranscripts(transcripts, false);
    }

    public List<RelatedMarker> getWithdrawnTranscripts() {
        if (transcripts == null || transcripts.size() == 0) {
            return null;
        }

        return TranscriptService.getSortedTranscripts(transcripts, true);
    }

    public List<RelatedMarker> getWithdrawnList() {
        if (withdrawnTranscripts == null) {
            return null;
        }
        if (withdrawnTranscripts == null) {
            withdrawnTranscripts = getWithdrawnTranscripts();
        }

        if (withdrawnTranscripts == null) {
            return null;
        }

        List<RelatedMarker> withdrawnlist = new ArrayList<>(withdrawnTranscripts.size());
        withdrawnlist.addAll(withdrawnTranscripts);
        return withdrawnlist;
    }

    public List<RelatedMarker> getNonWithdrawnList() {
        if (nonWithdrawnTranscripts == null) {
            nonWithdrawnTranscripts = getNonWithdrawnTranscripts();
        }

        if (nonWithdrawnTranscripts == null) {
            return null;
        }

        List<RelatedMarker> nonWithdrawnlist = new ArrayList<>(nonWithdrawnTranscripts.size());
        nonWithdrawnlist.addAll(nonWithdrawnTranscripts);
        return nonWithdrawnlist;
    }

}
