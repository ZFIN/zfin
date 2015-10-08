package org.zfin.sequence;

import org.zfin.orthology.Ortholog;


public class OrthologueDBLink extends DBLink {

    private Ortholog ortholog;

    public Ortholog getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(Ortholog ortholog) {
        this.ortholog = ortholog;
    }
}
