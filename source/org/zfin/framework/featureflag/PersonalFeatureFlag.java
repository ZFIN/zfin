package org.zfin.framework.featureflag;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.profile.Person;

import javax.persistence.*;

@Entity
@Table(name = "zdb_personal_feature_flag")
@Setter
@Getter
@JsonView(View.API.class)
public class PersonalFeatureFlag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "zpff_pk_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "zpff_person_zdb_id")
    private Person person;

    @Column(name = "zpff_flag_name")
    private String flagName;

    @Column(name = "zpff_enabled")
    private boolean enabled;
}
