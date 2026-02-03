package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single expression structure, consisting of a superterm (AO), a subterm, a stage in which
 * the superterm is defined and a boolean
 */
@Entity
@Table(name = "expression_pattern_infrastructure")
@Getter
@Setter
@AttributeOverrides({
    @AttributeOverride(name = "superterm", column = @Column(name = "xpatinf_superterm_zdb_id")),
    @AttributeOverride(name = "subterm", column = @Column(name = "xpatinf_subterm_zdb_id"))
})
@AssociationOverrides({
    @AssociationOverride(name = "superterm", joinColumns = @JoinColumn(name = "xpatinf_superterm_zdb_id")),
    @AssociationOverride(name = "subterm", joinColumns = @JoinColumn(name = "xpatinf_subterm_zdb_id"))
})
public class ExpressionStructure extends PostComposedEntity {

    @Id
    @GeneratedValue(generator = "ExpressionStructure")
    @GenericGenerator(name = "ExpressionStructure",
        strategy = "org.zfin.database.ZdbIdGenerator",
        parameters = {
            @Parameter(name = "type", value = "XPATINF"),
            @Parameter(name = "insertActiveData", value = "true")
        })
    @Column(name = "xpatinf_zdb_id")
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatinf_curator_zdb_id")
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatinf_pub_zdb_id")
    private Publication publication;

    @Column(name = "xpatinf_date")
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "xpatinf_quality_term_zdb_id")
    private GenericTerm eapQualityTerm;

    @Column(name = "xpatinf_tag")
    private String tag;

    @Column(name = "xpatinf_expression_found")
    private boolean expressionFound = true;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getSuperterm().getTermName());
        if (getSubterm() != null)
            builder.append(": " + getSubterm().getTermName());
        if (eapQualityTerm != null)
            builder.append(": " + eapQualityTerm.getTermName() + ": " + tag);
        return builder.toString();
    }

/*
    public int compareTo(ExpressionStructure o) {
        if (!(o instanceof ExpressionStructure))
            throw new RuntimeException("Comparable class not of type PhenotypeStructure");

        if (!superterm.equals(o.getSuperterm()))
            return superterm.getTermName().compareToIgnoreCase(o.getSuperterm().getTermName());
        if (subterm != null && o.getSubterm() == null)
            return 1;
        if (subterm == null && o.getSubterm() != null)
            return -1;
        if (subterm != null && o.getSubterm() != null)
            return subterm.getTermName().compareToIgnoreCase(o.getSubterm().getTermName());
        return 0;

    }
*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionStructure that = (ExpressionStructure) o;

        if (publication != null ? !publication.equals(that.publication) : that.publication != null) return false;
        if (subterm != null ? !subterm.equals(that.subterm) : that.subterm != null) return false;
        if (superterm != null ? !superterm.equals(that.superterm) : that.superterm != null) return false;
        if (eapQualityTerm != null ? !eapQualityTerm.equals(that.eapQualityTerm) : that.eapQualityTerm != null)
            return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (expressionFound != that.isExpressionFound()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = superterm != null ? superterm.hashCode() : 0;
        result = 31 * result + (subterm != null ? subterm.hashCode() : 0);
        result = 31 * result + (publication != null ? publication.hashCode() : 0);
        result = 29 * result + (eapQualityTerm != null ? eapQualityTerm.hashCode() : 0);
        result = 29 * result + (tag != null ? tag.hashCode() : 0);
        result = 29 * result + (expressionFound ? 51 : 0);
        return result;
    }

    public boolean isEap() {
        return eapQualityTerm != null;
    }

}
