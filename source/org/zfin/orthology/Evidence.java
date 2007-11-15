package org.zfin.orthology;

/**
 * User: giles
 * Date: Jul 26, 2006
 */

import java.util.Set;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * Business object used to hold a set of evidence codes for an orthologous gene.
 */
public class Evidence  implements Serializable {
    private Set<EvidenceCode> codes;

    public void setCodes(Set<EvidenceCode> codes) {
        this.codes = codes;
    }

    public Set<EvidenceCode> getCodes() {
        return codes;
    }
}
