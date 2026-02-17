package org.zfin.datatransfer.go;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "marker_go_term_evidence_annotation_organization")
public class GafOrganization {

    public enum OrganizationEnum {
        ZFIN("ZFIN"),
        FP_INFERENCES("FP Inferences"),
        GOA("GOA"),
        NOCTUA("Noctua"),
        PAINT("PAINT"),
        UNIPROT("UniProt"),
        ;

        private String value;

        private OrganizationEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public static OrganizationEnum getType(String type) {
            for (OrganizationEnum t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No OrganizationEnum named " + type + " found.");
        }
    }

    @Id
    @Column(name = "mrkrgoevas_pk_id")
    private long id;
    @Column(name = "mrkrgoevas_annotation_organization", nullable = false)
    private String organization;
    @Column(name = "mrkrgoevas_definition", nullable = false)
    private String definition;
    @Column(name = "mrkrgoevas_organization_url")
    private String url;

}
