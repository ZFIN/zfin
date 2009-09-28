package org.zfin.marker.presentation;

import org.zfin.expression.Figure;
import org.zfin.marker.MarkerStatistic;

import java.util.Set;
import java.text.ChoiceFormat;

/**
 * This business object defines a single gene, the publications and the images.
 */
public class ExpressedGeneDisplay {

    private MarkerStatistic markerStat;

    private ChoiceFormat figureChoice = new ChoiceFormat("0#figures|1#figure|2#figures");
    private ChoiceFormat imageChoice = new ChoiceFormat("0#images|1#image|2#images");
    private ChoiceFormat publicationChoice = new ChoiceFormat("0#sources|1#publication|2#sources");

    public ExpressedGeneDisplay(MarkerStatistic markerStat) {
        this.markerStat = markerStat;
    }

    /**
     * Calculates the number of figures found in all publications that are annotated
     * to the gene.
 * @return Number of figures as a string.
     */
    public String getNumberOfFigures() {
        int numberOfPublication = markerStat.getNumberOfFigures();
        return numberOfPublication + " " + figureChoice.format(numberOfPublication);
    }

    public String getNumberOfPublications() {
        int numberOfPublication = markerStat.getNumberOfPublications();
        return numberOfPublication + " " + publicationChoice.format(numberOfPublication);
    }

    /**
     * Calculates the number of images found in all images from all publications for the gene.
 * @return Number of images as a String
     */
    public String getNumberOfImages() {
        Set<Figure> figures = markerStat.getGene().getFigures();
        // calculate if the number is not set.
        int numberOfImages = 0;
        if (figures != null) {
            for (Figure fig : figures) {
                numberOfImages += fig.getImages().size();
            }
        }
        return numberOfImages + " " + imageChoice.format(numberOfImages);
    }


    public MarkerStatistic getMarkerStat() {
        return markerStat;
    }
}
