package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Disease model entity:
 */
@Setter
@Getter
@Entity
@Table(name = "disease_annotation")
public class DiseaseAnnotation implements EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DiseaseAnnotation")
    @GenericGenerator(name = "DiseaseAnnotation",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "DAT"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "dat_zdb_id")
    @JsonView(View.API.class)
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "dat_term_zdb_id")
    @JsonView(View.API.class)
    private GenericTerm disease;

    @ManyToOne
    @JoinColumn(name = "dat_source_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "dat_evidence_term_zdb_id")
    private GenericTerm evidenceCode;

    @OneToMany(mappedBy = "diseaseAnnotation")
    private List<DiseaseAnnotationModel> diseaseAnnotationModel;

    @JsonView(View.API.class)
    public List<Fish> getFishList() {
        return diseaseAnnotationModel.stream().map(model -> model.getFishExperiment().getFish()).collect(Collectors.toList());
    }

    @JsonView(View.API.class)
    public List<Experiment> getEnvironmentList() {
        return diseaseAnnotationModel.stream().map(model -> model.getFishExperiment().getExperiment()).collect(Collectors.toList());
    }

    @Override

    public String getAbbreviation() {
        return disease.getTermName();
    }

    @Override
    public String getAbbreviationOrder() {
        return disease.getTermName();
    }

    @Override
    public String getEntityType() {
        return "Disease Model";
    }

    @Override
    public String getEntityName() {
        return disease.getTermName();
    }

    @JsonView(View.API.class)
    public String getEvidenceCodeString() {
        if (getEvidenceCode() == null) {
            return "";
        }
        return DTOConversionService.evidenceCodeIdToAbbreviation(evidenceCode.getZdbID());
    }

}
