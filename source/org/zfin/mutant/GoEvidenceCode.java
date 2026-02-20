package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.zfin.framework.api.View;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "go_evidence_code")
public class GoEvidenceCode {

    // enum is it root.dto, atleast for now

    @Id
    @Column(name = "goev_code")
    @JsonView(View.API.class)
    private String code;
    @JsonView(View.API.class)
    @Column(name = "goev_name")
    private String name;
    @Column(name = "goev_display_order")
    private Integer order;

}
