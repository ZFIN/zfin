package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "feature_mutation_details")
public class MutationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fmd_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "fmd_dna_mutation_term_zdb_id")
    private GenericTerm mutationTerm;
    @ManyToOne
    @JoinColumn(name = "fmd_rna_consequence_term_zdb_id")
    private GenericTerm rnaConsequence;
    @ManyToOne
    @JoinColumn(name = "fmd_protein_consequence_term_zdb_id")
    private GenericTerm proteinConsequence;
    @Column(name = "fmd_transcript_exon_numnber")
    private int numberOfExons;
    @Column(name = "fmd_protein_change")
    private String proteinChange;
    @ManyToOne
    @JoinColumn(name = "fmd_feature_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public GenericTerm getMutationTerm() {
        return mutationTerm;
    }

    public void setMutationTerm(GenericTerm mutationTerm) {
        this.mutationTerm = mutationTerm;
    }

    public int getNumberOfExons() {
        return numberOfExons;
    }

    public void setNumberOfExons(int numberOfExons) {
        this.numberOfExons = numberOfExons;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public GenericTerm getProteinConsequence() {
        return proteinConsequence;
    }

    public void setProteinConsequence(GenericTerm proteinConsequence) {
        this.proteinConsequence = proteinConsequence;
    }

    public GenericTerm getRnaConsequence() {
        return rnaConsequence;
    }

    public void setRnaConsequence(GenericTerm rnaConsequence) {
        this.rnaConsequence = rnaConsequence;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
