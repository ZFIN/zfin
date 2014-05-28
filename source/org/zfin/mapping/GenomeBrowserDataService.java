package org.zfin.mapping;

import org.zfin.marker.Marker;

public interface GenomeBrowserDataService {

    public GenomeBrowserMetaData getGenomeBrowserMetaData();

    public String getBuild();

    public String getRelease();

    public GenomeLocation getGenomeLocation(Marker marker);
}
