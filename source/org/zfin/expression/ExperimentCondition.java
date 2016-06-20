package org.zfin.expression;

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

/**
 * Entity class that maps to experiment table.
 */
@Entity
@Table(name = "experiment_condition")
public class ExperimentCondition implements Comparable<ExperimentCondition>, EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
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

    /*
        @ManyToOne
        @JoinColumn(name = "expcond_go_cc_term_zdb_id")
    */
    @Transient
    private GenericTerm taxaonymTerm;

    @Transient
    private ConditionDataType conditionDataType;

    private static Logger logger = Logger.getLogger(ExperimentCondition.class);

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public ConditionDataType getConditionDataType() {
        return conditionDataType;
    }

    public void setConditionDataType(ConditionDataType conditionDataType) {
        this.conditionDataType = conditionDataType;
    }

    public boolean isChemicalCondition() {
        return (conditionDataType.getGroup().equalsIgnoreCase("chemical"));
    }

    public boolean isHeatShock() {
        return (conditionDataType.getName().equalsIgnoreCase("heat shock"));
    }

    @Override
    public int compareTo(ExperimentCondition o) {
        if (o == null)
            return -1;
        if (conditionDataType.compareTo(o.getConditionDataType()) != 0)
            return conditionDataType.compareTo(o.getConditionDataType());
        else
            return getZdbID().compareTo(o.getZdbID());
    }

    public GenericTerm getZecoTerm() {
        return zecoTerm;
    }

    public void setZecoTerm(GenericTerm zecoTerm) {
        this.zecoTerm = zecoTerm;
    }

    public GenericTerm getAoTerm() {
        return aoTerm;
    }

    public void setAoTerm(GenericTerm aoTerm) {
        this.aoTerm = aoTerm;
    }

    public GenericTerm getGoCCTerm() {
        return goCCTerm;
    }

    public void setGoCCTerm(GenericTerm goCCTerm) {
        this.goCCTerm = goCCTerm;
    }

    public GenericTerm getTaxaonymTerm() {
        return taxaonymTerm;
    }

    public void setTaxaonymTerm(GenericTerm taxaonymTerm) {
        this.taxaonymTerm = taxaonymTerm;
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
}
