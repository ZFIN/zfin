package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "pub_correspondence_need_resolution")
public class CorrespondenceResolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pcnres_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pcnres_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "pcnres_pcnrt_id")
    private CorrespondenceResolutionType resolutionType;

}
