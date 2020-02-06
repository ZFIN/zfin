package org.zfin.ontology;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Subset object. Each term can have zero, one or more subset definitions, i.e.
 * the terms belongs to a certain subset of the complete ontology.
 * E.g. relational_slim for 'fused with'
 */
@Entity
@Table(name = "ontology_subset")
public class Subset implements Serializable {

    public static final String RELATIONAL_SLIM = "relational_slim";
    // from the go ontology
    public static final String GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS = "gocheck_do_not_annotate";
    public static final String GO_CHECK_DO_NOT_USE_FOR_MANUAL_ANNOTATIONS = "gocheck_do_not_manually_annotate";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "osubset_pk_id")
    private long id;
    @Column(name = "osubset_subset_name")
    private String internalName;
    // this is also called definition in the database
    @Column(name = "osubset_subset_definition")
    private String name;
    @ManyToOne
    @JoinColumn(name = "osubset_ont_id")
    private OntologyMetadata metaData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_subset",
            joinColumns = {@JoinColumn(name = "termsub_subset_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "termsub_term_zdb_id", nullable = false, updatable = false)})
    private Set<GenericTerm> terms;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OntologyMetadata getMetaData() {
        return metaData;
    }

    public void setMetaData(OntologyMetadata metaData) {
        this.metaData = metaData;
    }

    public Set<GenericTerm> getTerms() {
        return terms;
    }

    public void setTerms(Set<GenericTerm> terms) {
        this.terms = terms;
    }

    public static boolean isUseForAnnotations(String name) {
        if (name.equals(GO_CHECK_DO_NOT_USE_FOR_MANUAL_ANNOTATIONS) ||
                name.equals(GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS)) {
            return false;
        }
        return true;
    }
}
