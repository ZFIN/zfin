package org.zfin.publication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doi_attempts")
public class DOIAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doia_pk_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doia_pub_zdb_id", nullable = false)
    private Publication publication;

    @Column(name = "doia_num_attempts", nullable = false)
    private int numAttempts;

    public int addAttempt() {
        return ++numAttempts;
    }
}
