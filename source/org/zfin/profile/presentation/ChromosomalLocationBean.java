package org.zfin.profile.presentation;

import org.zfin.mapping.GenomeLocation;

public class ChromosomalLocationBean {

    String entityID;
    String assembly;
    String chromosome;
    String startLocation;
    String endLocation;

    public static ChromosomalLocationBean fromGenomeLocation(GenomeLocation persistedLocation) {
        ChromosomalLocationBean clBean = new ChromosomalLocationBean();
        clBean.setEntityID(persistedLocation.getEntityID());
        clBean.setAssembly(persistedLocation.getAssembly());
        clBean.setChromosome(persistedLocation.getChromosome());
        clBean.setStartLocation(persistedLocation.getStart().toString());
        clBean.setEndLocation(persistedLocation.getEnd().toString());
        return clBean;
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

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public GenomeLocation toGenomeLocation() {
        GenomeLocation genomeLocation = new GenomeLocation();
        genomeLocation.setAssembly(this.getAssembly());
        genomeLocation.setChromosome(this.getChromosome());

        try {
            int startLocation = Integer.parseInt(this.getStartLocation());
            genomeLocation.setStart(startLocation);
        } catch (NumberFormatException nfe) {
            //don't set start location
        }

        try {
            int endLocation = Integer.parseInt(this.getEndLocation());
            genomeLocation.setEnd(endLocation);
        } catch (NumberFormatException nfe) {
            //don't set end location
        }
        return genomeLocation;
    }
}
