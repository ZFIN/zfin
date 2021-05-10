package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MarkerRelationshipEditMetadata {

    private String type;
    private boolean is1to2;
    private String relatedMarkerTypeGroup;

}
