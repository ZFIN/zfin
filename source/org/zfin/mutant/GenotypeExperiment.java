package org.zfin.mutant;

import org.zfin.expression.Experiment;

/**
 * Created by IntelliJ IDEA.
 * User: Xiang Shao
 * Date: Jun 15, 2007
 * Time: 5:02:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenotypeExperiment {
    private String zdbID;
    private Experiment experiment;
    private Genotype genotype;


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

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }
}
