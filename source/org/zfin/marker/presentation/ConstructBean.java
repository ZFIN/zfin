package org.zfin.marker.presentation;

import org.zfin.mutant.presentation.GenotypeInformation;

import java.util.List;

/**
 */
public class ConstructBean extends MarkerBean{

    private List<GenotypeInformation> transgenicLines;

    public List<GenotypeInformation> getTransgenicLines() {
        return transgenicLines;
    }

    public void setTransgenicLines(List<GenotypeInformation> transgenicLines) {
        this.transgenicLines = transgenicLines;
    }
}
