package org.zfin.marker;

import jakarta.persistence.*;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.Term;

@Entity
@Table(name = "probe_library")
public class ProbeLibrary {
    @Id
    @Column(name = "probelib_zdb_id")
    private String zdbID;

    @Column(name = "probelib_name")
    private String name;

    @Column(name = "probelib_url")
    private String url;

    @Column(name = "probelib_species")
    private String species;

    @Column(name = "probelib_non_zfin_strain_name")
    private String nonZfinStrain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "probelib_strain_zdb_id")
    private Genotype strain;

    @Column(name = "probelib_sex")
    private String sex;

    @Column(name = "probelib_non_zfin_tissue_name")
    private String nonZfinTissue;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = org.zfin.ontology.GenericTerm.class)
    @JoinColumn(name = "probelib_tissue_zdb_id")
    private Term tissue;

    @Column(name = "probelib_host")
    private String host;

    @Column(name = "probelib_restriction_sites")
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
