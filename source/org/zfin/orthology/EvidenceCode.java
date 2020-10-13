package org.zfin.orthology;

import java.io.Serializable;

/**
 * Business object used to hold an evidence code which gets associated with an orthologous gene.
 */
public class EvidenceCode implements Comparable<EvidenceCode>, Serializable {

    private String code;
    private Integer order;
    private String name;


    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int compareTo(EvidenceCode e) {
        return code.compareTo(e.getCode());
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }

        if (!(o instanceof EvidenceCode)) {
            return false;
        }

        EvidenceCode that = (EvidenceCode) o;
        return !(code != null ? !code.equals(that.getCode()) : that.getCode() != null);

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    public String toString() {
        return "EvidenceCode{" +
                "code='" + code + '\'' +
                '}';
    }

}
