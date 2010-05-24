package org.zfin.marker;

import org.zfin.mutant.Genotype;
import org.zfin.ontology.Term;

/**
 */
public class ProbeLibrary {
    private String zdbID;
    private String name;
    private String url;
    private String species;
    private String nonZfinStrain;
    private Genotype strain;
    private String sex;
    private String nonZfinTissue;
    private Term tissue;
    private String host;
    private String restrictionSites;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getNonZfinStrain() {
        return nonZfinStrain;
    }

    public void setNonZfinStrain(String nonZfinStrain) {
        this.nonZfinStrain = nonZfinStrain;
    }

    public Genotype getStrain() {
        return strain;
    }

    public void setStrain(Genotype strain) {
        this.strain = strain;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNonZfinTissue() {
        return nonZfinTissue;
    }

    public void setNonZfinTissue(String nonZfinTissue) {
        this.nonZfinTissue = nonZfinTissue;
    }

    public Term getTissue() {
        return tissue;
    }

    public void setTissue(Term tissue) {
        this.tissue = tissue;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRestrictionSites() {
        return restrictionSites;
    }

    public void setRestrictionSites(String restrictionSites) {
        this.restrictionSites = restrictionSites;
    }
}
