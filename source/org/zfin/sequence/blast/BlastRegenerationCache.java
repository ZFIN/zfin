package org.zfin.sequence.blast;

 import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "blastdb_regen_content")
public class BlastRegenerationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brc_pk_id", nullable = false)
    private long id;

    @Column(name = "brc_acc_num", nullable = false)
    private String accession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brc_blastdb_zdb_id", nullable = false)
    private Database blastDatabase;
}
