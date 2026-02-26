package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.jbrowse.presentation.GenomeBrowserImage;
import org.zfin.mapping.presentation.BrowserLink;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Setter
@Getter
public class GeneBean extends MarkerBean {

    boolean hasChimericClone;
    private List<LinkDisplay> plasmidDBLinks;
    private List<LinkDisplay> pathwayDBLinks;
    private TreeSet<BrowserLink> locations;
    private Set<BrowserLink> transcriptLocations;

    GenomeBrowserImage refSeqLocations;
}
