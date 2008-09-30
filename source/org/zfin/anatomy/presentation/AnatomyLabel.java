package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.GoTerm;
import org.zfin.publication.Publication;

import java.text.ChoiceFormat;
import java.util.*;

public class AnatomyLabel implements Comparable<AnatomyLabel> {

    private AnatomyItem anatomyItem;
    private GoTerm cellularComponent;
    private String aoAndPostCompostTerm;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Publication> publications;
    private Set<Figure> figures;

    private SortedSet<ExpressionAssay> assays;
    private SortedSet<Marker> genes;
    private boolean figureWithImage;
    private boolean notAllFiguresTextOnly;

    public static String TEXT_ONLY = "text only";

    public AnatomyLabel(AnatomyItem anatomyItem, GoTerm goTerm) {
        setAnatomyItem(anatomyItem);
        setCellularComponent(goTerm);
        if (anatomyItem == null && goTerm == null)
          setAoAndPostCompostTerm("");
        else if (goTerm == null)
          setAoAndPostCompostTerm(anatomyItem.getName());
        else
          setAoAndPostCompostTerm(anatomyItem.getName() + goTerm.getName());
        publications = new HashSet<Publication>();
        figures = new HashSet<Figure>();
        assays = new TreeSet<ExpressionAssay>();
        genes = new TreeSet<Marker>();
    }

    public AnatomyLabel(AnatomyItem anatomyItem, GoTerm goTerm, DevelopmentStage startStage, DevelopmentStage endStage) {
        setAnatomyItem(anatomyItem);
        setCellularComponent(goTerm);
        if (goTerm == null)
          setAoAndPostCompostTerm(anatomyItem.getName());
        else
          setAoAndPostCompostTerm(anatomyItem.getName() + goTerm.getName());
        setStartStage(startStage);
        setEndStage(endStage);
        publications = new HashSet<Publication>();
        figures = new HashSet<Figure>();
        assays = new TreeSet<ExpressionAssay>();
        genes = new TreeSet<Marker>();
    }

    static public ChoiceFormat figureChoice = new ChoiceFormat("0#figures|1#figure|2#figures");
    static public ChoiceFormat publicationChoice = new ChoiceFormat("0#publications|1#publication|2#publications");

    public AnatomyItem getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
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
            return TEXT_ONLY;
    }

    public Publication getSinglePublication() {
        for (Publication pub : publications) {
            return pub;
        }
        return null;
    }

    public int compareTo(AnatomyLabel anotherDisplay) {
        if (anotherDisplay == null)
            return 1;
        int result = anatomyItem.compareTo(anotherDisplay.getAnatomyItem());
        if(result == 0) {
          if (cellularComponent == null && anotherDisplay.getCellularComponent() == null)
            return 0;
          else if (cellularComponent == null)
            return -1;
          else if (anotherDisplay.getCellularComponent() == null)
            return 1;
          else
            return cellularComponent.compareTo(anotherDisplay.getCellularComponent());
        } else {
          return result;
        }
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
            mrkrType.setName("GENE");
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
        return figureWithImage;
    }

    public void setFigureWithImage(boolean figureWithImage) {
        this.figureWithImage = figureWithImage;
    }

    public GoTerm getCellularComponent() {
        return cellularComponent;
    }

    public void setCellularComponent(GoTerm cellularComponent) {
        this.cellularComponent = cellularComponent;
    }

    public String getAoAndPostCompostTerm() {
        return aoAndPostCompostTerm;
    }

    public void setAoAndPostCompostTerm(String aoAndPostCompostTerm) {
        this.aoAndPostCompostTerm = aoAndPostCompostTerm;
    }

    public boolean isNotAllFiguresTextOnly() {
        return notAllFiguresTextOnly;
    }

    public void setNotAllFiguresTextOnly(boolean notAllFiguresTextOnly) {
        this.notAllFiguresTextOnly = notAllFiguresTextOnly;
    }
}
