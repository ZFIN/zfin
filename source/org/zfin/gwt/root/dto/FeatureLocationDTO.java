package org.zfin.gwt.root.dto;


import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.ontology.GenericTerm;


public class FeatureLocationDTO implements IsSerializable {

    private String zdbID;
    private String chromosome;
    private Integer positionStart;
    private Integer positionEnd;
    private String assembly;
    private GenericTerm evidence;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getPositionStart() {
        return positionStart;
    }

    public void setPositionStart(Integer positionStart) {
        this.positionStart = positionStart;
    }

    public Integer getPositionEnd() {
        return positionEnd;
    }

    public void setPositionEnd(Integer positionEnd) {
        this.positionEnd = positionEnd;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }

    public GenericTerm getEvidence() {
        return evidence;
    }

    public void setEvidence(GenericTerm evidence) {
        this.evidence = evidence;
    }
}