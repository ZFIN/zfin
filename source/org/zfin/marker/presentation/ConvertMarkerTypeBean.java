package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;

import java.util.List;

@Setter
@Getter
public class ConvertMarkerTypeBean {

    private String zdbIDToConvert;
    private Marker marker;
    private String newMarkerTypeName;
    private String newZdbId;
    private List<MarkerType> availableTypes;
}
