package org.zfin.mapping;

import lombok.Getter;
import lombok.Setter;

import org.zfin.marker.Marker;

/**
 * Feature Location .
 */
@Setter
@Getter
public class MarkerLocation extends Location {

    private Marker marker;

}
