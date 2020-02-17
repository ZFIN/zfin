package org.zfin.marker.presentation;

import org.zfin.orthology.EvidenceCode;
import org.zfin.publication.Publication;

import java.util.Set;
import java.util.TreeSet;

public class ProteinDomainValue {

    private String ipValue;
    private String present;

    public String getIpValue() {
        return ipValue;
    }

    public void setIpValue(String ipValue) {
        this.ipValue = ipValue;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }
}
