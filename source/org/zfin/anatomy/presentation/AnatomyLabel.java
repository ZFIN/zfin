package org.zfin.anatomy.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionStatement;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.text.ChoiceFormat;
import java.util.*;

public class AnatomyLabel implements Comparable<AnatomyLabel> {

    private ExpressionResult expressionResult;
    private ExpressionStatement expressionStatement;

    private Set<Publication> publications;
    private Set<Figure> figures;

    private SortedSet<ExpressionAssay> assays;
    private SortedSet<Marker> genes;
    private boolean figureWithImage;
    private boolean notAllFiguresTextOnly;

    public AnatomyLabel(ExpressionResult result) {
        expressionResult = result;

        if (result.getEntity() == null || result.getEntity().getSuperterm() == null)
            throw new NullPointerException("No Term provided.");

        expressionStatement = new ExpressionStatement();
        expressionStatement.setEntity(expressionResult.getEntity());
        expressionStatement.setExpressionFound(expressionResult.isExpressionFound());

        publications = new HashSet<Publication>();
        figures = new HashSet<Figure>();
        assays = new TreeSet<ExpressionAssay>();
        genes = new TreeSet<Marker>();
    }

     public static final ChoiceFormat figureChoice = new ChoiceFormat("0#figures|1#figure|2#figures");
    static public ChoiceFormat publicationChoice = new ChoiceFormat("0#publications|1#publication|2#publications");

    public ExpressionResult getExpressionResult() {
        return expressionResult;
    }

    public ExpressionStatement getExpressionStatement() {
        return expressionStatement;
    }

    //KLS - I'm leaving the lower level getters in for convenience, but in the long run they may not be necessary
    public Term getSuperterm() {
        return expressionResult.getEntity().getSuperterm();
    }

    public Term getSubterm() {
        return expressionResult.getEntity().getSubterm();
    }

    public DevelopmentStage getStartStage() {
        return expressionResult.getStartStage();
    }

    public DevelopmentStage getEndStage() {
        return expressionResult.getEndStage();
    }

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public int getNumberOfPublications() {
        return publications.size();
    }

    public int getNumberOfFigures() {
        return figures.size();
    }

    public String getNumberOfPublicationsDisplay() {
        int numberOfPublication = publications.size();
        return numberOfPublication + " " + publicationChoice.format(numberOfPublication);
    }

    public String getNumberOfFiguresDisplay() {
        if (notAllFiguresTextOnly)
            return getNumberOfFigures() + " " + figureChoice.format(getNumberOfFigures());
        else
            return Figure.Type.TOD.getName();
    }

    public Publication getSinglePublication() {
        if (CollectionUtils.isNotEmpty(publications)) {
            return publications.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * TODO: Someone needs to add doc to this.  I fixed the NPE in the code (fogbugz 5732), but I'm unclear as to
     * what the function of it should be.
     *
     * KLS -- coming across this, it seems to sort based only on the terms and not on the stages, I'm converting
     *        it to just compare the ExpressionStatement belonging to the label...
     *
     * @param anotherDisplay
     * @return
     */
    public int compareTo(AnatomyLabel anotherDisplay) {
        if (anotherDisplay == null) {
            return 1;
        }
        return expressionStatement.compareTo(anotherDisplay.getExpressionStatement());
    }

    public Figure getSingleFigure() {
        for (Figure figure : figures)
            return figure;
        return null;
    }

    public SortedSet<ExpressionAssay> getAssays() {
        return assays;
    }

    public void setAssays(SortedSet<ExpressionAssay> assays) {
        this.assays = assays;
    }

    public SortedSet<Marker> getGenes() {
        return genes;
    }

    public void setGenes(SortedSet<Marker> genes) {
        this.genes = genes;
    }

    public List<Marker> getAntigenGenes() {
        List<Marker> markers = new ArrayList<Marker>();
        for (Marker gene : genes) {
            Marker marker = new Marker();
            MarkerType mrkrType = new MarkerType();
            mrkrType.setName(Marker.Type.GENE.name());
            mrkrType.setType(Marker.Type.GENE);
            Set<Marker.TypeGroup> typeGroup = new HashSet<Marker.TypeGroup>();
            typeGroup.add(Marker.TypeGroup.GENEDOM);
            mrkrType.setTypeGroups(typeGroup);
            marker.setZdbID(gene.getZdbID());
            marker.setName(gene.getName());
            marker.setAbbreviation(gene.getAbbreviation());
            marker.setMarkerType(mrkrType);
            markers.add(marker);
        }
        return markers;
    }

    public boolean isFigureWithImage() {
        if (figures == null || figures.isEmpty()) {
            return false;
        }

        boolean withImg = false;
        for (Figure fig : figures) {
             if (fig.getImages() != null && fig.getImages().size() > 0) {
                 withImg = true;
                 break;
             }
        }
        return withImg;
    }

    public void setFigureWithImage(boolean figureWithImage) {
        this.figureWithImage = figureWithImage;
    }

    public String getAoAndPostCompostTerm() {
        if (expressionResult.getEntity().getSubterm() == null)
            return expressionResult.getEntity().getSubterm().getTermName();
        return expressionResult.getEntity().getSuperterm().getTermName()
                + expressionResult.getEntity().getSubterm().getTermName();
    }

    public boolean isNotAllFiguresTextOnly() {
        return notAllFiguresTextOnly;
    }

    public void setNotAllFiguresTextOnly(boolean notAllFiguresTextOnly) {
        this.notAllFiguresTextOnly = notAllFiguresTextOnly;
    }
}
