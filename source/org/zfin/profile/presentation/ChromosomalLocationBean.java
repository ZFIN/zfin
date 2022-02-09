package org.zfin.profile.presentation;

import org.zfin.mapping.GenomeLocation;

public class ChromosomalLocationBean {
    Long ID;
    String entityID;
    String assembly;
    String chromosome;
    Integer startLocation;
    Integer endLocation;

    public static ChromosomalLocationBean fromGenomeLocation(GenomeLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setID(persistedLocation.getID());
        clBean.setEntityID(persistedLocation.getEntityID());
        clBean.setAssembly(persistedLocation.getAssembly());
        clBean.setChromosome(persistedLocation.getChromosome());
        clBean.setStartLocation(persistedLocation.getStart());
        clBean.setEndLocation(persistedLocation.getEnd());
        return clBean;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Integer startLocation) {
        this.startLocation = startLocation;
    }

    public Integer getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Integer endLocation) {
        this.endLocation = endLocation;
    }

    public GenomeLocation toGenomeLocation() {
        GenomeLocation genomeLocation = new GenomeLocation();
        genomeLocation.setAssembly(this.getAssembly());
        genomeLocation.setChromosome(this.getChromosome());

        try {
            int startLocation = this.getStartLocation();
            genomeLocation.setStart(startLocation);
        } catch (NumberFormatException nfe) {
            //don't set start location
        }

        try {
            int endLocation = this.getEndLocation();
            genomeLocation.setEnd(endLocation);
        } catch (NumberFormatException nfe) {
            //don't set end location
        }
        return genomeLocation;
    }
}
