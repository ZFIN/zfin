package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "pub_correspondence_need_reason")
public class CorrespondenceNeedReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pcnr_pk_id")
    private long id;

    @Column(name = "pcnr_name")
    private String name;

    @Column(name = "pcnr_order")
    private long order;

}
