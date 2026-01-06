package org.zfin.sequence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "zfin_accession_number")
public class ZfinAccessionNumber {

    @Id
    @Column(name = "za_acc_num", nullable = false)
    private String zAccNum;

}
