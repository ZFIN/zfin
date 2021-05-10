package org.zfin.marker.agr;

import java.util.Objects;

public class UberonSlimTermDTO {

    private String uberonTerm;

    public UberonSlimTermDTO(String uberonTerm) {
        this.uberonTerm = uberonTerm;
    }

    public String getUberonTerm() {
        return uberonTerm;
    }

    public void setUberonTerm(String uberonTerm) {
        this.uberonTerm = uberonTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UberonSlimTermDTO that = (UberonSlimTermDTO) o;
        return Objects.equals(uberonTerm, that.uberonTerm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uberonTerm);
    }
}
