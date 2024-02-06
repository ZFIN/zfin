package org.zfin.orthology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;

@Setter
@Getter
public class Ortholog implements Comparable, Serializable {

    @JsonView(View.OrthologyAPI.class)
    private String zdbID;
    @JsonView(View.OrthologyAPI.class)
    private Marker zebrafishGene;
    private Set<OrthologEvidence> evidenceSet;
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;
    private SortedSet<OrthologExternalReference> externalReferenceList;

    @JsonView(View.OrthologyAPI.class)
    private String name;
    @JsonView(View.OrthologyAPI.class)
    private String symbol;
    @JsonView(View.OrthologyAPI.class)
    private String chromosome;
    @JsonView(View.OrthologyAPI.class)
    private org.zfin.Species organism;
    private boolean obsolete;


    public int compareTo(Object o) {
        //todo: ordering method doesn't use a zero padded attribute
        return getNcbiOtherSpeciesGene().getAbbreviation().compareTo(((Ortholog) o).getNcbiOtherSpeciesGene().getAbbreviation());
    }

    public String toString() {
        String lineFeed = System.getProperty("line.separator");
        return "ORTHOLOG" + lineFeed +
                "zdbID: " + zdbID + lineFeed +
                "abbreviation: " + ncbiOtherSpeciesGene.getAbbreviation() + lineFeed +
                "name: " + ncbiOtherSpeciesGene.getName() + lineFeed +
                "gene: " + zebrafishGene + lineFeed +
                "organism: " + ncbiOtherSpeciesGene.getOrganism().toString() + lineFeed + evidenceSet;
    }

}

