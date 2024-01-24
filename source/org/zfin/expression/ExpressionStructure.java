package org.zfin.expression;

import lombok.Data;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single expression structure, consisting of a superterm (AO), a subterm, a stage in which
 * the superterm is defined and a boolean
 */
@Data
public class ExpressionStructure extends PostComposedEntity {

    private String zdbID;
    private Person person;
    private Publication publication;
    private Date date;
    private GenericTerm eapQualityTerm;
    private String tag;
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
