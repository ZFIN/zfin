package org.zfin.datatransfer.go;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

/**
 * mapping of ECO ID to GO evidence Code
 * http://wiki.geneontology.org/index.php/Evidence_Code_Ontology_(ECO)
 */

@Entity
@Table(name = "eco_go_mapping")
public class EcoGoEvidenceCodeMapping  {


    @Id
    @Column (name = "egm_pk_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "egm_go_evidence_code")
    protected String evidenceCode;
    @ManyToOne()
    @JoinColumn(name = "egm_term_zdb_id")
    private GenericTerm ecoTerm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public GenericTerm getEcoTerm() {
        return ecoTerm;
    }

    public void setEcoTerm(GenericTerm ecoTerm) {
        this.ecoTerm = ecoTerm;
    }

}
