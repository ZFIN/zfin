package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
public class NcbiOtherSpeciesGeneDTO {

    private String ID;
    private String name;
    @JsonView(View.OrthologyAPI.class)
    private String abbreviation;
    private String chromosome;
    private String position;
    @JsonView(View.OrthologyAPI.class)
    private String organism;

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
