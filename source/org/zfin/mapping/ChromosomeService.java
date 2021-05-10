package org.zfin.mapping;

import java.util.List;

import static org.zfin.mapping.MappingService.AMBIGUOUS;

public class ChromosomeService<T extends GenomeLocation> {

    private List<T> genomeLocationList;
    private boolean trustedValue = false;
    private String chromosomeNumber = "";

    public ChromosomeService(List<T> genomeLocationList) {
        this.genomeLocationList = genomeLocationList;
        init();
    }

    private void init() {
        String locationDisplay = MappingService.getChromosomeLocationDisplay(genomeLocationList);
	if(locationDisplay == null)
		return;
        if (!locationDisplay.equals(AMBIGUOUS) && !locationDisplay.isEmpty()) {
            trustedValue = true;
            chromosomeNumber = locationDisplay;
        }
    }

    public boolean isTrustedValue() {
        return trustedValue;
    }

    public String getChromosomeNumber() {
        return chromosomeNumber;
    }
}




