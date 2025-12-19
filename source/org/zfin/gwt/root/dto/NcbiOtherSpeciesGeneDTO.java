package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
public class NcbiOtherSpeciesGeneDTO {

    @JsonView(View.OrthologyAPI.class)
    private String ID;
    private String name;
    @JsonView(View.OrthologyAPI.class)
    private String abbreviation;
    private String chromosome;
    private String position;
    @JsonView(View.OrthologyAPI.class)
    private String organism;

    public String getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public String getChromosome() {
        return this.chromosome;
    }

    public String getPosition() {
        return this.position;
    }

    public String getOrganism() {
        return this.organism;
    }

    @JsonView(View.OrthologyAPI.class)
    public void setID(String ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView(View.OrthologyAPI.class)
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @JsonView(View.OrthologyAPI.class)
    public void setOrganism(String organism) {
        this.organism = organism;
    }
}
