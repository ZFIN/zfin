package org.zfin.marker.presentation;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.text.ChoiceFormat;
import java.util.List;

/**
 * Container to hold Gene and EST info.
 */
public class HighQualityProbe {

    private Marker gene;
    // rename to probe
    private Marker subGene;
    private Publication publication;
    private List<Figure> figures;
    private List<Publication> publications;
    private int numberOfImages = -1;

    private ChoiceFormat cf = new ChoiceFormat("0#figures|1#figure|2#figures");
    private ChoiceFormat cimages = new ChoiceFormat("0#images|1#image|2#images");

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Marker getSubGene() {
        return subGene;
    }

    public void setSubGene(Marker subGene) {
        this.subGene = subGene;
    }

    public void setFigures(List<Figure> figures) {
        this.figures = figures;
    }

    public String getNumberOfFigures() {
        return figures.size() + " " + cf.format(figures.size());
    }

    public String getNumberOfImages() {
        // calculate if the number is not set.
        if (numberOfImages == -1) {
            numberOfImages = 0;
            if (figures != null) {
                for (Figure fig : figures) {
                    numberOfImages += fig.getImages().size();
                }
            }
        }
        return numberOfImages + " " + cimages.format(numberOfImages);
    }

    public Publication getPublication() {
        return publication;
    }

    public int getNumberOfPublications() {
        if (publications == null)
            return 0;
        return publications.size();
    }

    public Publication getProbePublication() {
        if (publications == null)
            return null;

        if (publications.size() != 1)
            throw new RuntimeException("Found more than one publication for probe: " + subGene.getAbbreviation());

        return publications.get(0);
    }


    public void setPublication(Publication publication) {
        this.publication = publication;
    }


    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }


    public List<Publication> getPublications() {
        return publications;
    }

    public List<Figure> getFigures() {
        return figures;
    }

    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.get(0);
    }


}
