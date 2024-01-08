package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "clone_problem_type")
public class CloneProblem {
    @Id
    @Column(name = "cpt_type")
    @JsonView(View.API.class)
    private String type;
}
