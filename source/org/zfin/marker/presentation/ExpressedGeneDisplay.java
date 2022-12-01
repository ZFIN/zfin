package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.marker.MarkerStatistic;

import java.text.ChoiceFormat;

/**
 * This business object defines a single gene, the publications and the images.
 */
public class ExpressedGeneDisplay {

    @JsonView(View.ExpressedGeneAPI.class)
    private MarkerStatistic markerStat;

    private ChoiceFormat figureChoice = new ChoiceFormat("0#figures|1#figure|2#figures");
    private ChoiceFormat imageChoice = new ChoiceFormat("0#images|1#image|2#images");
    private ChoiceFormat publicationChoice = new ChoiceFormat("0#sources|1#publication|2#sources");

    public ExpressedGeneDisplay(MarkerStatistic markerStat) {
        this.markerStat = markerStat;
    }

    public String getNumberOfPublications() {
        int numberOfPublication = markerStat.getNumberOfPublications();
        return numberOfPublication + " " + publicationChoice.format(numberOfPublication);
    }

    public MarkerStatistic getMarkerStat() {
        return markerStat;
    }
}
