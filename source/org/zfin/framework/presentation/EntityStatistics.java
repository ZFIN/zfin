package org.zfin.framework.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public abstract class EntityStatistics {

    protected List<Publication> publications;
    protected int numberOfPublications = -1;
    @JsonView(View.API.class)
    private Set<Marker> genes = new HashSet<>();
    private Set<Figure> figs = new HashSet<>();
    private Set<Publication> pubs = new HashSet<>();
    private Set<Image> images = new HashSet<>();
    @JsonView(View.API.class)
    private boolean hasData;
    @JsonView({View.API.class, View.ExpressedGeneAPI.class})
    private boolean hasImages;

    @JsonView({View.API.class, View.ExpressedGeneAPI.class})
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


    /**
     * Override this method to retrieve the publication for a given entity statistics.
     *
     * @return pagination result.
     */
    protected abstract PaginationResult<Publication> getPublicationPaginationResult();


    public Publication getSinglePublication() {
        if (publications == null) {
            // make sure the query is run.
            getNumberOfPublications();
        }
        if (publications == null || publications.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return publications.iterator().next();
    }

    public int getNumberOfPubs() {
        return pubs.size();
    }

    public Publication getSinglePub() {
        if (pubs == null || pubs.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return pubs.iterator().next();
    }

    @JsonView({View.API.class, View.ExpressedGeneAPI.class})
    public Publication getFirstPublication() {
        if (getNumberOfPublications() == 0) {
            return null;
        } else {
            return getPublicationPaginationResult().getPopulatedResults().get(0);
        }
    }

    @JsonView(View.API.class)
    @Transient
    public int getNumberOfFigures() {
        return figs.size();
    }

    @JsonView(View.API.class)
    public Figure getFirstFigure() {
        if (getNumberOfFigures() == 0) {
            return null;
        } else {
            return figs.iterator().next();
        }
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

    public Figure getFigure() {
        if (figs == null || figs.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figs.iterator().next();
    }

    public int getNumberOfImages() {
        if (figs == null || figs.isEmpty()) {
            return 0;
        }
        if (images != null && !images.isEmpty()) {
            return images.size();
        }
        Set<Image> imgs = new HashSet<>();
        for (Figure fig : figs) {
            imgs.addAll(fig.getImages());
        }
        return imgs.size();
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

    public boolean getHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    @JsonView(View.API.class)
    public boolean isImgInFigure() {
        return false;
    }

}
