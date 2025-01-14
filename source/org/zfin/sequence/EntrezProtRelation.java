package org.zfin.sequence;

import lombok.Getter;
import lombok.Setter;
import org.zfin.Species;
import jakarta.persistence.*;

@Setter
@Getter

@Entity
@Table(name = "entrez_to_protein")
public class EntrezProtRelation implements Comparable<EntrezProtRelation> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ep_pk_id", nullable = false)
    private long epID;

    @Column(name = "ep_organism_common_name")
    private Species.Type organism;

    @Column(name = "ep_protein_acc_num", nullable = false)
    private String proteinAccNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ep_protein_acc_num", referencedColumnName = "accbk_acc_num", insertable = false, updatable = false)
    private Accession accession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ep_entrez_acc_num", nullable = false)
    private Entrez entrezAccession;

    public int compareTo(EntrezProtRelation o) {
        return (o.getEntrezAccession().getEntrezAccNum().compareTo(this.getEntrezAccession().getEntrezAccNum()));  //To change body of implemented methods use File | Settings | File Templates.
    }
}
