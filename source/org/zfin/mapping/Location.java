package org.zfin.mapping;


import jakarta.persistence.*;

import org.hibernate.annotations.DiscriminatorFormula;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.ontology.GenericTerm;

import java.util.Set;

/**
 * Feature Location .
 */
@Setter
@Getter
@Table(name = "sequence_feature_chromosome_location")
@Entity
@DiscriminatorFormula(" (CASE get_obj_type(sfcl_feature_zdb_id) " +
                        " WHEN 'ALT' THEN  'Feat' " +
                        " ELSE             'Mark' " +
                        " END) ")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "Location")
    @GenericGenerator(name = "Location",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "SFCL"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "sfcl_zdb_id")
    protected String zdbID;

    @Column(name = "sfcl_start_position")
    protected Integer startLocation;

    @Column(name = "sfcl_end_position")
    protected Integer endLocation;

    @Column(name = "sfcl_chromosome")
    protected String chromosome;

    @Column(name = "sfcl_assembly")
    protected String assembly;

    @ManyToOne
    @JoinColumn(name = "sfcl_evidence_code")
    protected GenericTerm locationEvidence;

    // orphanRemoval=true so removing from this set DELETEs the
    // record_attribution row instead of NULL'ing recattrib_data_zdb_id —
    // the column is NOT NULL and the dissociate-via-UPDATE that Hibernate
    // would otherwise emit fails with a constraint violation.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id")
    protected Set<RecordAttribution> references;

    @Column(name = "sfcl_chromosome_reference_accession_number")
    protected String referenceSequenceAccessionNumber;

    public void removeReference(RecordAttribution reference) {
        this.references.remove(reference);
    }
}
