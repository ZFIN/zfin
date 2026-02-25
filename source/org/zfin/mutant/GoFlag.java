package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Getter
@Setter
@Entity
@Immutable
@Table(name = "go_flag")
public class GoFlag {

    @Id
    @Column(name = "gflag_name")
    private String name;
    @Column(name = "gflag_display_order")
    private Integer displayOrder;

}
