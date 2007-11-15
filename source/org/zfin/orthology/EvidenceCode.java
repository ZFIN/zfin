package org.zfin.orthology;

import java.io.Serializable;

/**
 * Business object used to hold an evidence code which gets associated with an orthologous gene.
 */
public class EvidenceCode implements Comparable<EvidenceCode>, Serializable {

    private String code;

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int compareTo(EvidenceCode e) {
        return code.compareTo(e.getCode());
    }


    public String toString() {
        return "EvidenceCode{" +
                "code='" + code + '\'' +
                '}';
    }
}
