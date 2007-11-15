package org.zfin.sequence;

import org.zfin.orthology.Orthologue;


public class OrthologueDBLink extends DBLink {

    private Orthologue orthologue;

    public Orthologue getOrthologue() {
        return orthologue;
    }

    public void setOrthologue(Orthologue orthologue) {
        this.orthologue = orthologue;
    }
}
