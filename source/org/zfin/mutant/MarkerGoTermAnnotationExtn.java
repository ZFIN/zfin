package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "marker_go_term_annotation_extension")
public class MarkerGoTermAnnotationExtn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mgtae_pk_id")
    private Long id;

    @Column(name = "mgtae_relationship_term_zdb_id")
    private String relationshipTerm;

    @Column(name = "mgtae_term_text")
    private String identifierTermText;

    @Column(name = "mgtae_identifier_term_zdb_id")
    private String identifierTerm;

    @Column(name = "mgtae_dblink_zdb_id")
    private String annotExtnDBLink;

    @ManyToOne
    @JoinColumn(name = "mgtae_extension_group_id")
    private MarkerGoTermAnnotationExtnGroup annotExtnGroupID;

    public MarkerGoTermAnnotationExtn(String relationshipTerm, String identifierTermText) {
        this.relationshipTerm = relationshipTerm;
        this.identifierTermText = identifierTermText;
    }

    // Do not use this (only used by hibernate)
    public MarkerGoTermAnnotationExtn() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerGoTermAnnotationExtn that = (MarkerGoTermAnnotationExtn) o;

        if (!relationshipTerm.equals(that.relationshipTerm)) return false;
        if (!identifierTermText.equals(that.identifierTermText)) return false;
        if (annotExtnDBLink != null ? !annotExtnDBLink.equals(that.annotExtnDBLink) : that.annotExtnDBLink != null)
            return false;
        return identifierTerm != null ? identifierTerm.equals(that.identifierTerm) : that.identifierTerm == null;
    }

    @Override
    public int hashCode() {
        int result = relationshipTerm.hashCode();
        result = 31 * result + identifierTermText.hashCode();
        result = 31 * result + (annotExtnDBLink != null ? annotExtnDBLink.hashCode() : 0);
        result = 31 * result + (identifierTerm != null ? identifierTerm.hashCode() : 0);
        return result;
    }
}
