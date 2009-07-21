package org.zfin.expression;

import org.zfin.marker.Marker;

/**
 * ToDo: Please add documentation for this class.
 */
public class ExperimentCondition {

    private String zdbID;
    private Experiment experiment;
    private Marker morpholino;
    private String value;
    private ExperimentUnit unit;

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

    public Marker getMorpholino() {
        return morpholino;
    }

    public void setMorpholino(Marker morpholino) {
        this.morpholino = morpholino;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ExperimentUnit getUnit() {
        return unit;
    }

    public void setUnit(ExperimentUnit unit) {
        this.unit = unit;
    }
}
