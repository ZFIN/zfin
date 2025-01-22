
package org.zfin.sequence;


import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Setter
@Getter

@Entity
@Table(name = "protein_to_pdb")
public class ProteinToPDB {

    @Id
    @Column(name = "ptp_pk_id")
    private long ID;
    @Column(name = "ptp_uniprot_id")
    private String uniProtID;
    @Column(name = "ptp_pdb_id")
    private String pdbID;

}
