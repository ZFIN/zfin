package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "pub_correspondence_need_resolution_type")
public class CorrespondenceResolutionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pcnrt_pk_id")
    private long id;

    @Column(name = "pcnrt_name")
    private String name;

    @Column(name = "pcnrt_order")
    private long order;

}
