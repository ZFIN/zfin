package org.zfin.orthology;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Business object used to hold an evidence code which gets associated with an orthologous gene.
 */
@Getter
@Setter
@Entity
@Table(name = "ortholog_evidence_code")
public class EvidenceCode implements Comparable<EvidenceCode>, Serializable {

    @Id
    @Column(name = "oevcode_code", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.API.class)
    private String code;

    @Column(name = "oevcode_order", nullable = false)
    @JsonView(View.API.class)
    private Integer order;

    @Column(name = "oevcode_name", nullable = false)
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

        if (!(o instanceof EvidenceCode that)) {
            return false;
        }

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
