package org.zfin.marker.presentation;

import org.zfin.feature.Feature;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.GenotypeFishResult;

import java.util.List;

/**
 */
public class ConstructBean extends MarkerBean {

    private List<Feature> transgenics;
    private List<GenotypeFishResult> fish;
    private List<Marker> regulatoryRegions;
    private List<Marker> codingSequences;
    private int numberOfTransgeniclines;
    private List<ControlledVocab> species;

    public List<Feature> getTransgenics() {
        return transgenics;
    }

    public void setTransgenics(List<Feature> transgenics) {
        this.transgenics = transgenics;
    }

    public List<GenotypeFishResult> getFish() {
        return fish;
    }

    public void setFish(List<GenotypeFishResult> fish) {
        this.fish = fish;
    }

    public List<Marker> getRegulatoryRegions() {
        return regulatoryRegions;
    }

    public void setRegulatoryRegions(List<Marker> regulatoryRegions) {
        this.regulatoryRegions = regulatoryRegions;
    }

    public List<Marker> getCodingSequences() {
        return codingSequences;
    }

    public void setCodingSequences(List<Marker> codingSequences) {
        this.codingSequences = codingSequences;
    }

    public int getNumberOfTransgeniclines() {
        return numberOfTransgeniclines;
    }

    public void setNumberOfTransgeniclines(int numberOfTransgeniclines) {
        this.numberOfTransgeniclines = numberOfTransgeniclines;
    }

    public List<ControlledVocab> getSpecies() {
        return species;
    }

    public void setSpecies(List<ControlledVocab> species) {
        this.species = species;
    }
}
