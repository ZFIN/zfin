package org.zfin.profile;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "email_privacy_preference")
public class EmailPrivacyPreference {

    @Id
    @Column(name = "epp_pk_id")
    private Long id;

    @Column(name = "epp_name")
    private String name;

    @Column(name = "epp_description")
    private String description;

    @Column(name = "epp_order")
    private Integer order;

    public String toString() {
        return name;
    }
}