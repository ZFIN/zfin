package org.zfin.marker.presentation;

import lombok.Data;
import org.zfin.jbrowse.presentation.JBrowse2Image;
import org.zfin.marker.Marker;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;

@Data
public class RelatedTranscriptDisplay  {

    private List<RelatedMarker> transcripts;
    private List<RelatedMarker> nonWithdrawnTranscripts;
    private List<RelatedMarker> withdrawnTranscripts;
    private Marker gene;
    private JBrowse2Image gbrowseImage;

    public RelatedTranscriptDisplay() {
        transcripts = new ArrayList<>();
    }

    public void add(RelatedMarker rm) {
        transcripts.add(rm);
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
