package org.zfin.expression;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class that maps to experiment table.
 */
@Entity
@Table(name = "experiment_condition")
@Setter
@Getter
public class ExperimentCondition implements Comparable<ExperimentCondition>, EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ExperimentCondition")
    @GenericGenerator(name = "ExperimentCondition",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "EXPCOND"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "expcond_zdb_id")
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "expcond_exp_zdb_id")
    private Experiment experiment;
    @ManyToOne
    @JoinColumn(name = "expcond_zeco_term_zdb_id")
    private GenericTerm zecoTerm;
    @ManyToOne
    @JoinColumn(name = "expcond_ao_term_zdb_id")
    private GenericTerm aoTerm;
    @ManyToOne
    @JoinColumn(name = "expcond_go_cc_term_zdb_id")
    private GenericTerm goCCTerm;
    @ManyToOne
    @JoinColumn(name = "expcond_taxon_term_zdb_id")
    private GenericTerm taxaonymTerm;
    @ManyToOne
    @JoinColumn(name = "expcond_chebi_term_zdb_id")
    private GenericTerm chebiTerm;
    @ManyToOne
    @JoinColumn(name = "expcond_spatial_term_zdb_id")
    private GenericTerm spatialTerm;

    private static Logger logger = LogManager.getLogger(ExperimentCondition.class);

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public boolean isChemicalCondition() {
        return (zecoTerm != null && zecoTerm.getTermName().contains("chemical"));
    }

    public boolean isHeatShock() {
        return (zecoTerm != null && zecoTerm.getTermName().equalsIgnoreCase("heat shock"));
    }

    @Override
    public int compareTo(ExperimentCondition o) {
        if (o == null || o.getZecoTerm() == null) {
            return -1;
        }
        if (zecoTerm != null && !zecoTerm.getOboID().equals(o.getZecoTerm().getOboID())) {
            return zecoTerm.getTermName().compareToIgnoreCase(o.getZecoTerm().getTermName());
        }
        return 0;
    }

    @Override
    public String getAbbreviation() {
        return experiment.getName();
    }

    @Override
    public String getAbbreviationOrder() {
        return experiment.getName();
    }

    @Override
    public String getEntityType() {
        return "Environment";
    }

    @Override
    public String getEntityName() {
        return experiment.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentCondition that = (ExperimentCondition) o;

        if (zecoTerm != null ? !zecoTerm.equals(that.zecoTerm) : that.zecoTerm != null) return false;
        if (aoTerm != null ? !aoTerm.equals(that.aoTerm) : that.aoTerm != null) return false;
        if (goCCTerm != null ? !goCCTerm.equals(that.goCCTerm) : that.goCCTerm != null) return false;
        if (chebiTerm != null ? !chebiTerm.equals(that.chebiTerm) : that.chebiTerm != null) return false;
        return !(taxaonymTerm != null ? !taxaonymTerm.equals(that.taxaonymTerm) : that.taxaonymTerm != null);

    }

    @Override
    public int hashCode() {
        int result = zecoTerm != null ? zecoTerm.hashCode() : 0;
        result = 31 * result + (aoTerm != null ? aoTerm.hashCode() : 0);
        result = 31 * result + (goCCTerm != null ? goCCTerm.hashCode() : 0);
        result = 31 * result + (taxaonymTerm != null ? taxaonymTerm.hashCode() : 0);
        return result;
    }

    @JsonView(View.API.class)
    public String getDisplayName() {
        if (zecoTerm == null) {
            return "";
        }
        String displayName = zecoTerm.getTermName();
        if (chebiTerm != null) {
            displayName += ": " + chebiTerm.getTermName();
        }
        if (goCCTerm != null) {
            displayName += ": " + goCCTerm.getTermName();
        }
        if (aoTerm != null) {
            displayName += ": " + aoTerm.getTermName();
        }
        if (taxaonymTerm != null) {
            displayName += ": " + taxaonymTerm.getTermName();
        }
        return displayName;
    }

    public List<GenericTerm> getAllTerms() {
        if (zecoTerm == null) {
            return null;
        }
        List<GenericTerm> terms = new ArrayList<>();
        terms.add(zecoTerm);
        if (chebiTerm != null) {
            terms.add(chebiTerm);
        }
        if (goCCTerm != null) {
            terms.add(goCCTerm);
        }
        if (aoTerm != null) {
            terms.add(aoTerm);
        }
        if (taxaonymTerm != null) {
            terms.add(taxaonymTerm);
        }
        return terms;
    }
}
