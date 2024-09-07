package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.PhenotypeService;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convenient class to show statistics about phenotypes related to a given AO term..
 */
@Setter
@Getter
@Entity
@Table(name = "UI.TERM_PHENOTYPE_DISPLAY")
@JsonPropertyOrder({"fish", "anatomyItem", "numberOfFigures", "imgInFigure", "firstFigure", "phenotypeObserved"})
public class FishStatistics extends EntityStatistics {

    @Id
    @JsonView(View.API.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tpd_id", nullable = false)
    private long id;

    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpd_fish_zdb_id")
    private Fish fish;
    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpd_fig_zdb_id")
    private Figure figure;
    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpd_pub_zdb_id")
    private Publication publication;
    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "UI.PHENOTYPE_ZFIN_ASSOCIATION",
            // TODO (ZFIN-9354): hibernate migration change, confirm logic still valid
            // Fixes this error: org.hibernate.AnnotationException: Join column '...' on collection property 'org.zfin...' must be defined with the same insertable and updatable attributes
        joinColumns = {@JoinColumn(name = "pza_phenotype_id", nullable = false, updatable = false, insertable = false)},
        inverseJoinColumns = {@JoinColumn(name = "pza_gene_zdb_id", nullable = false, updatable = false, insertable = false)})
    private Set<Marker> affectedGenes;
    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToMany(fetch=FetchType.LAZY)
    @JoinTable(name = "UI.PHENOTYPE_WAREHOUSE_ASSOCIATION",
            // TODO (ZFIN-9354): hibernate migration change, confirm logic still valid
            // Fixes this error: org.hibernate.AnnotationException: Join column '...' on collection property 'org.zfin...' must be defined with the same insertable and updatable attributes
        joinColumns = {@JoinColumn(name = "pwa_phenotype_id", nullable = false, updatable = false, insertable = false)},
        inverseJoinColumns = {@JoinColumn(name = "pwa_phenotype_warehouse_id", nullable = false, updatable = false, insertable = false)})
    private Set<PhenotypeStatementWarehouse> phenotypeStatements;
    @JsonView(View.ExpressedGeneAPI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpd_term_zdb_id")
    private GenericTerm term;

    @JsonView(View.ExpressedGeneAPI.class)
    @Column(name = "tpd_fig_count")
    private int numberOfFigs;

    @JsonView(View.ExpressedGeneAPI.class)
    @Column(name = "tpd_pub_count")
    private int numberOfPubs;

    @Column(name = "tpd_fish_search")
    private String fishSearch;

    @Column(name = "tpd_phenotype_search")
    private String phenotypeStatementSearch;

    @Column(name = "tpd_gene_search")
    private String geneSymbolSearch;

    @Transient
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet
    @Transient
    private Set<Publication> publicationSet = null; // null indicates that this has not been populated yet
    @Transient
    private boolean includeSubstructures;

    public FishStatistics() {
    }

    public FishStatistics(Fish fish) {
        this.fish = fish;
    }

    public FishStatistics(Fish fish, GenericTerm term) {
        this.fish = fish;
        this.term = term;
    }

    public FishStatistics(Fish fish, GenericTerm term, boolean includeSubstructures) {
        this.fish = fish;
        this.term = term;
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

    @Transient
    private int numberOfFigures = -1;

    @JsonView(View.ExpressedGeneAPI.class)
    public int getNumberOfFigures() {
        if (numberOfFigures >= 0)
            return numberOfFigures;
        if (figureResults == null) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByFishAndAnatomy(fish, term, includeSubstructures);
            if (publicationSet == null) {
                publicationSet = new TreeSet<>();
                for (Figure figure : figureResults.getPopulatedResults()) {
                    publicationSet.add(figure.getPublication());
                }
            }
        }
        numberOfFigures = figureResults.getTotalCount();
        return numberOfFigures;
    }

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

    /**
     * @return There should be a single figure per GenotypeStatistics
     */
    @JsonView(View.ExpressedGeneAPI.class)
    public Figure getFirstFigure() {
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByFishAndAnatomy(fish, term);
        }
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            return null;
        }
        return figureResults.getPopulatedResults().get(0);
    }

    public Set<Publication> getPublicationSet() {
        getNumberOfFigures();
        return publicationSet;
    }

    @JsonView(View.ExpressedGeneAPI.class)
    public Set<PhenotypeStatementWarehouse> getPhenotypeObserved() {
        Set<PhenotypeStatementWarehouse> phenotypeObserved = new TreeSet<>();
        phenotypeObserved.addAll(PhenotypeService.getPhenotypeObserved(fish, term, includeSubstructures));
        return phenotypeObserved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FishStatistics that)) return false;
        return getId() == that.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
