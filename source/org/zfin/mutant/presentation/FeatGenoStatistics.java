package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeFeature;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class FeatGenoStatistics {

    private Genotype genotype;
    private Feature feature;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet
    private PaginationResult<Figure> expfigureResults = null;
    private PaginationResult<Figure> morphfigureResults = null;
    private PaginationResult<Figure> imgfigureResults = null;
    private PaginationResult<Figure> expimgfigureResults = null;
    private int numberOfExpFigures;
    private int numberOfFigures;
    private int numberOfMorphFigures;
    private int numberOfImgFigures;
    private int numberOfImages;
    private int numberOfExpImgs;
    private Figure figure;
    private Figure imgfigure;
    private Figure expimg;
    private Figure expFigure;
    private Publication singlePublication;
    private Publication singleExpPublication;
    private Boolean isMorpholino;
    protected List<Publication> publications;
    protected List<Publication> exppublications;
    protected int numberOfPublications = -1;
    protected int numberOfExpPublications = -1;
    private Set<Marker> genes = new HashSet<Marker>();
    private Set<Figure> figs = new HashSet<Figure>();
    private Set<Publication> pubs = new HashSet<Publication>();
    private Set<Image> images = new HashSet<Image>();
    private Boolean isImage;
    private Boolean isImageExp;


    public FeatGenoStatistics(Genotype genotype, Feature feature) {
        this.genotype = genotype;
        this.feature = feature;
    }

    public Genotype getGenotype() {
        return genotype;
    }


    public int getNumberOfFigures() {
        if (figureResults == null) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGeno(genotype);
        }
        numberOfFigures = figureResults.getTotalCount();
        return numberOfFigures;

    }

    public int getNumberOfExpFigures() {
        if (expfigureResults == null) {
            expfigureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoExp(genotype);
        }
        numberOfExpFigures = expfigureResults.getTotalCount();
        return numberOfExpFigures;
    }

    public Boolean getIsMorpholino() {

        morphfigureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoMorph(genotype);
        numberOfMorphFigures = morphfigureResults.getTotalCount();
        if (numberOfMorphFigures != 0) {
            return isMorpholino = true;
        }
        return isMorpholino;
    }

    /**
     * @return There should be a single figure per GenotypeStatistics
     */
    public Figure getFigure() {
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGeno(genotype);
        }
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        }
        figure = figureResults.getPopulatedResults().get(0);
        return figure;
    }


    public Boolean getIsImage() {
        imgfigureResults = RepositoryFactory.getPublicationRepository().getFiguresByGeno(genotype);
        boolean thereIsImg = false;
        for (Figure fig : imgfigureResults.getPopulatedResults()) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }


    public Boolean getIsImageExp() {
        expimgfigureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoExp(genotype);
        boolean thereIsImg = false;
        for (Figure fig : expimgfigureResults.getPopulatedResults()) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }


    public Figure getExpFigure() {
        if (expfigureResults == null || expfigureResults.getTotalCount() != 1) {
            expfigureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoExp(genotype);
        }
        if (expfigureResults == null || expfigureResults.getTotalCount() != 1) {
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        }
        expFigure = expfigureResults.getPopulatedResults().get(0);
        return expFigure;
    }


    protected PaginationResult<Publication> getPublicationPaginationResult() {
        PublicationRepository repository = RepositoryFactory.getPublicationRepository();
        return repository.getPublicationsWithFiguresbyGeno(genotype);

    }

    protected PaginationResult<Publication> getExpPublicationPaginationResult() {
        PublicationRepository repository = RepositoryFactory.getPublicationRepository();
        return repository.getPublicationsWithFiguresbyGenoExp(genotype);

    }


    public SortedSet<Marker> getAffectedMarkers() {
        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
        SortedSet<Marker> markers = new TreeSet<Marker>();
        for (GenotypeFeature feat : features) {
            Feature feature = feat.getFeature();
            Set<FeatureMarkerRelationship> rels = feature.getFeatureMarkerRelations();
            SortedSet<FeatureMarkerRelationship> affectedGenes = new TreeSet<FeatureMarkerRelationship>();
        for (FeatureMarkerRelationship ftrmrkrRelation : rels) {
            if (ftrmrkrRelation != null)
                if (ftrmrkrRelation.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    affectedGenes.add(ftrmrkrRelation);
                }
        }
            for (FeatureMarkerRelationship rel : affectedGenes) {
                 Marker marker = rel.getMarker();
                // Only add true genes
                if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    markers.add(marker);
                }
            }

        }
        return markers;
    }


    public int getNumberOfPublications() {
        if (numberOfPublications == -1) {
            PaginationResult<Publication> pubs = getPublicationPaginationResult();
            if (pubs == null) {
                numberOfPublications = 0;
            } else {
                numberOfPublications = pubs.getTotalCount();
                if (numberOfPublications == 1)
                    publications = pubs.getPopulatedResults();
            }
        }
        return numberOfPublications;
    }

    public int getNumberOfExpPublications() {
        if (numberOfExpPublications == -1) {
            PaginationResult<Publication> pubs = getExpPublicationPaginationResult();
            if (pubs == null) {
                numberOfExpPublications = 0;
            } else {
                numberOfExpPublications = pubs.getTotalCount();
                if (numberOfExpPublications == 1)
                    exppublications = pubs.getPopulatedResults();
            }
        }
        return numberOfExpPublications;
    }

    public Publication getSinglePublication() {
        if (publications == null) {
            // make sure the query is run.
            getNumberOfPublications();
        }
        if (publications == null || publications.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        singlePublication = publications.iterator().next();
        return singlePublication;
    }

    public Publication getSingleExpPublication() {
        if (exppublications == null) {
            // make sure the query is run.
            getNumberOfExpPublications();
        }
        if (exppublications == null || exppublications.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        singleExpPublication = exppublications.iterator().next();
        return singleExpPublication;
    }

    public int getNumberOfPubs() {
        return pubs.size();
    }

    public Publication getSinglePub() {
        if (pubs == null || pubs.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return pubs.iterator().next();
    }

    public Publication getSingleExpPub() {
        if (pubs == null || pubs.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return pubs.iterator().next();
    }


    public void addFigure(Figure figure) {
        if (figure == null)
            return;

        figs.add(figure);
    }

    public void addPublication(Publication publication) {
        if (publication == null)
            return;

        pubs.add(publication);
    }


    public int getNumberOfImages() {
        numberOfImages = images.size();
        return numberOfImages;
    }

    public Image getImage() {
        if (images == null || images.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one image");
        return images.iterator().next();
    }

    public void addImage(Image image) {
        if (image == null)
            return;
        images.add(image);
    }

    public void addGene(Marker gene) {
        if (gene == null)
            return;
        genes.add(gene);
    }

    public Set<Marker> getGenes() {
        return genes;
    }


}
