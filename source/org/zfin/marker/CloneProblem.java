package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
