package org.zfin.orthology.repository;

import java.util.Set;

/**
 * Hibernate repository business object used to map most of the orthology information
*/
public class ZebrafishOrthologyHelper extends OrthologyHelper{

    private Set<OrthologyHelper> orthologies;

    public Set<OrthologyHelper> getOrthologies() {
        return orthologies;
    }

    public void setOrthologies(Set<OrthologyHelper> orthologies) {
        this.orthologies = orthologies;
    }
}
