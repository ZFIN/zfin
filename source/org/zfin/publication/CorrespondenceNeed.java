package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "pub_correspondence_need")
public class CorrespondenceNeed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pcn_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pcn_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "pcn_pcnr_id")
    private CorrespondenceNeedReason reason;

}
