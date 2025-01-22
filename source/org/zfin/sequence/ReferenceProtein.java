package org.zfin.sequence;

import lombok.Data;
import org.zfin.marker.Marker;

import jakarta.persistence.*;

@Entity
@Table(name = "REFERENCE_PROTEIN")
@Data
public class ReferenceProtein {

    @Id
    @Column(name = "rp_pk_id", columnDefinition = "serial")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne()
    @JoinColumn(name = "rp_dblink_zdb_id")
    private MarkerDBLink uniprotAccession;
    @ManyToOne()
    @JoinColumn(name = "rp_gene_zdb_id")
    private Marker gene;

    public ReferenceProtein(MarkerDBLink uniprotAccession, Marker gene) {
        this.uniprotAccession = uniprotAccession;
        this.gene = gene;
    }
}
