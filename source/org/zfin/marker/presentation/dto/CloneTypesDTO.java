package org.zfin.marker.presentation.dto;

import java.io.Serializable;
import java.util.List;

public class CloneTypesDTO implements Serializable {

    private List<String> vectorNames;
    private List<String> polymeraseNames;
    private List<String> probeLibraries;
    private List<String> digests;
    private List<String> cloneSites;
    private List<String> problemTypes;

    public List<String> getVectorNames() {
        return vectorNames;
    }

    public void setVectorNames(List<String> vectorNames) {
        this.vectorNames = vectorNames;
    }

    public List<String> getPolymeraseNames() {
        return polymeraseNames;
    }

    public void setPolymeraseNames(List<String> polymeraseNames) {
        this.polymeraseNames = polymeraseNames;
    }

    public List<String> getProbeLibraries() {
        return probeLibraries;
    }

    public void setProbeLibraries(List<String> probeLibraries) {
        this.probeLibraries = probeLibraries;
    }

    public List<String> getDigests() {
        return digests;
    }

    public void setDigests(List<String> digests) {
        this.digests = digests;
    }

    public List<String> getCloneSites() {
        return cloneSites;
    }

    public void setCloneSites(List<String> cloneSites) {
        this.cloneSites = cloneSites;
    }

    public List<String> getProblemTypes() {
        return problemTypes;
    }

    public void setProblemTypes(List<String> problemTypes) {
        this.problemTypes = problemTypes;
    }
}
