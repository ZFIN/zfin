package org.zfin.orthology;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.Set;
import java.util.TreeSet;

/**
 * A single orthology item, i.e.
 * orthologous to a set of species by virtue of a publication and evidence code.
 */
public class Orthology {

    private Publication publication;
    private Marker gene;
    private EvidenceCode evidenceCode;
    private Set<Species> orthologousSpeciesList = new TreeSet<>();

    public EvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(EvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Set<Species> getOrthologousSpeciesList() {
        return orthologousSpeciesList;
    }

    public void setOrthologousSpeciesList(Set<Species> orthologousSpeciesList) {
        this.orthologousSpeciesList = orthologousSpeciesList;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public void addOrthologousSpecies(String species) {
        if (StringUtils.isEmpty(species))
            throw new NullPointerException("No species passed in");

        Species spec = Species.getSpecies(species);
        orthologousSpeciesList.add(spec);
    }

    public boolean containsSpeciesString(String species) {
        if (StringUtils.isEmpty(species))
            return false;
        return containsSpecies(Species.getSpecies(species));
    }

    public boolean containsSpecies(Species species) {
        if (orthologousSpeciesList.size() == 0)
            return false;
        return orthologousSpeciesList.contains(species);
    }
}
