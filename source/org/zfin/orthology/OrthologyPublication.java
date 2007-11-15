package org.zfin.orthology;

import org.zfin.publication.*;
import java.util.List;

/**
 * User: giles
 * Date: Aug 1, 2006
 */

/**
 * Currently unused business object for holding evidence code information for a publication.
 */
public class OrthologyPublication extends Publication {
    private List<EvidenceCode> codes;

    public void setCodes(List<EvidenceCode> codes) {
        this.codes = codes;
    }

    public List<EvidenceCode> getCodes() {
        return codes;
    }

}
