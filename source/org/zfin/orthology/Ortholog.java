package org.zfin.orthology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import jakarta.persistence.*;

@Entity
@Table(name = "ortholog")
@Setter
@Getter
public class Ortholog implements Comparable, Serializable {

    @Id
    @Column(name = "ortho_zdb_id", nullable = false)
    @GeneratedValue(generator = "zdbIdGeneratorForOrtholog")
    @GenericGenerator(name = "zdbIdGeneratorForOrtholog",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "ORTHO"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @JsonView(View.OrthologyAPI.class)
    private String zdbID;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ortho_zebrafish_gene_zdb_id", nullable = false)
    @JsonView(View.OrthologyAPI.class)
    private Marker zebrafishGene;

    @OneToMany(mappedBy = "ortholog", fetch = FetchType.EAGER)
    private Set<OrthologEvidence> evidenceSet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ortho_other_species_ncbi_gene_id", nullable = false)
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;

    @OneToMany(mappedBy = "ortholog", fetch = FetchType.EAGER)
    private SortedSet<OrthologExternalReference> externalReferenceList;

    @Column(name = "ortho_other_species_name")
    @JsonView(View.OrthologyAPI.class)
    private String name;

    @Column(name = "ortho_other_species_symbol")
    @JsonView(View.OrthologyAPI.class)
    private String symbol;

    @Column(name = "ortho_other_species_chromosome")
    @JsonView(View.OrthologyAPI.class)
    private String chromosome;

    @Column(name = "ortho_other_species_ncbi_gene_is_obsolete")
    @JsonView(View.OrthologyAPI.class)
    private boolean obsolete;

    @ManyToOne
    @JoinColumn(name = "ortho_Other_species_taxid")
    private org.zfin.Species organism;

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
