package org.zfin.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.zfin.profile.Person;

import jakarta.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "updates")
public class Updates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upd_pk_id")
    private Long ID;
    @Column(name = "rec_id")
    private String recID;
    @ManyToOne
    @JoinColumn(name = "submitter_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Person submitter;
    @Column(name = "field_name")
    private String fieldName;
    @Column(name = "old_value")
    private String oldValue;
    @Column(name = "new_value")
    private String newValue;
    @Column(name = "comments")
    private String comments;
    @Column(name = "upd_when")
    private Date whenUpdated;
    @Column(name = "submitter_name")
    private String submitterName;

}
