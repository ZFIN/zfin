package org.zfin.mutant.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;


@Setter
@Getter
public class PhenotypeDisplay implements Comparable<PhenotypeDisplay> {

    @JsonView(View.FigureAPI.class)
    private PhenotypeStatementWarehouse phenoStatement;

    @JsonView(View.API.class)
    private List<Publication> publications;

    private SortedMap<Publication, SortedSet<Figure>> figuresPerPub;

    public PhenotypeDisplay(PhenotypeStatementWarehouse phenoStatement) {
        this.phenoStatement = phenoStatement;
    }

    @JsonView(View.FigureAPI.class)
    public Experiment getExperiment() {
        return phenoStatement.getPhenotypeWarehouse().getFishExperiment().getExperiment();
    }

    public int compareTo(PhenotypeDisplay o) {
        if (phenoStatement.equals(o.getPhenoStatement())) {
            return getExperiment().compareTo(o.getExperiment());
        } else {
            if (phenoStatement.getQuality() != null && phenoStatement.getEntity().compareTo(o.getPhenoStatement().getEntity()) == 0) {
                if (phenoStatement.getQuality().compareTo(o.getPhenoStatement().getQuality()) == 0)
                    return phenoStatement.getTag().compareTo(o.getPhenoStatement().getTag());
                else
                    return phenoStatement.getQuality().compareTo(o.getPhenoStatement().getQuality());
            } else {
                return phenoStatement.getEntity().compareTo(o.getPhenoStatement().getEntity());
            }
        }
    }

    @JsonView(View.API.class)
    public int getNumberOfPublications() {
        return getPublications().size();
    }

    @JsonView(View.API.class)
    public int getNumberOfFigures() {
        return getPublications().stream().map(publication -> publication.getFigures().size()).reduce(0, Integer::sum);
    }

    @JsonView(View.API.class)
    public Figure getFirstFigure() {
        return getPublications().get(0).getFigures().iterator().next();
    }

    @JsonView(View.API.class)
    public Publication getFirstPublication() {
        return getPublications().get(0);
    }

}