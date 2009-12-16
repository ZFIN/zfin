package org.zfin.marker.presentation;

import org.zfin.marker.Transcript;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class RelatedTranscriptDisplay extends TreeSet<RelatedMarker> {

    public RelatedTranscriptDisplay() {
        super(new RelatedTranscriptNameSort());
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
        ArrayList<RelatedMarker> list = new ArrayList<RelatedMarker>();
        list.addAll(this);
        return list;
    }

}
