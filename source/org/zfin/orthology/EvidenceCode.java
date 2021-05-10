package org.zfin.orthology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import java.io.Serializable;

/**
 * Business object used to hold an evidence code which gets associated with an orthologous gene.
 */
@Getter
@Setter
public class EvidenceCode implements Comparable<EvidenceCode>, Serializable {

    @JsonView(View.API.class)
    private String code;
    @JsonView(View.API.class)
    private Integer order;
    @JsonView(View.API.class)
    private String name;

    public int compareTo(EvidenceCode e) {
        return code.compareTo(e.getCode());
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
