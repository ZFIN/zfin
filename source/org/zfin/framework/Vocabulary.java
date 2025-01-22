package org.zfin.framework;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.framework.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "vocabulary")
public class Vocabulary extends BaseEntity {

    @Id
    @JsonView(View.API.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "v_id", nullable = false)
    private long id;

    @JsonView(View.API.class)
    @Column(name = "v_name")
    private String name;

    @JsonView(View.API.class)
    @Column(name = "v_description")
    private String description;

    @JsonView(View.API.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    @Column(name = "v_date_created")
    private LocalDateTime startDate;


}
