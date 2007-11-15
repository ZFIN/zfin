package org.zfin.orthology;

import org.zfin.marker.Marker;

import java.util.List;
import java.io.Serializable;

/**
 * User: giles
 * Date: Jul 13, 2006
 * Time: 2:38:50 PM
 */

/**
 * High level business object which contains all of the information for an orthologous gene for a given species.
 */
public class OrthologySpecies  implements Serializable {

    private Marker marker;
    private Species species;
    private List<OrthologyItem> items;

    public void setSpecies(Species species) {
        this.species = species;
    }

    public Species getSpecies() {
        return species;
    }

    public void setItems(List<OrthologyItem> items) {
        this.items = items;
    }

    public List<OrthologyItem> getItems() {
        return items;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
