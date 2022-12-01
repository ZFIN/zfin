package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.Set;
import java.util.TreeSet;

/**
 * Convenient class to show statistics about phenotypes related to a given AO term..
 */
@JsonPropertyOrder({"fish", "anatomyItem", "numberOfFigures", "imgInFigure", "firstFigure", "phenotypeObserved"})
public class FishStatistics extends EntityStatistics {

    @JsonView(View.ExpressedGeneAPI.class)
    private Fish fish;
    @JsonView(View.ExpressedGeneAPI.class)
    private GenericTerm anatomyItem;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet
    private Set<Publication> publicationSet = null; // null indicates that this has not been populated yet
    private boolean includeSubstructures;

    public FishStatistics(Fish fish) {
        this.fish = fish;
    }

    public FishStatistics(Fish fish, GenericTerm anatomyItem) {
        this.fish = fish;
        this.anatomyItem = anatomyItem;
    }

    public FishStatistics(Fish fish, GenericTerm anatomyItem, boolean includeSubstructures) {
        this.fish = fish;
        this.anatomyItem = anatomyItem;
        this.includeSubstructures = includeSubstructures;
    }

    public Fish getFish() {
        return fish;
    }

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        if(publicationSet == null)
            return null;
        return new PaginationResult<>(publicationSet.stream().toList());
    }

    private int numberOfFigures = -1;

    @JsonView(View.ExpressedGeneAPI.class)
    public boolean isImgInFigure() {
        if (figureResults == null || figureResults.getTotalCount() == 0) {
            return false;
        }
        boolean thereIsImg = false;
        for (Figure fig : figureResults.getPopulatedResults()) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }

    @JsonView(View.ExpressedGeneAPI.class)
    public Set<PhenotypeStatementWarehouse> getPhenotypeObserved() {
        Set<PhenotypeStatementWarehouse> phenotypeObserved = new TreeSet<>();
        phenotypeObserved.addAll(PhenotypeService.getPhenotypeObserved(fish, anatomyItem, includeSubstructures));
        return phenotypeObserved;
    }


}
