package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;

@Setter
@Getter
@Entity
@Table(name = "disease_annotation_model")
public class DiseaseAnnotationModel implements ZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "damo_pk_id")
    @JsonView(View.API.class)
    private long ID;

    @ManyToOne
    @JoinColumn(name = "damo_dat_zdb_id")
    @JsonView(View.API.class)
    private DiseaseAnnotation diseaseAnnotation;

    @ManyToOne
    @JoinColumn(name = "damo_genox_zdb_id")
    @JsonView(View.API.class)
    private FishExperiment fishExperiment;

    @Override
    public String getZdbID() {
        return String.valueOf(ID);
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}