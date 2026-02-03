package org.zfin.ontology;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

/**
 * A class to group superterm and subterm into a single object,
 * intended to be used as a component in expression and phenotype
 * records.
 *
 * This class is annotated as @MappedSuperclass to allow entities like
 * ExpressionStructure to extend it and inherit the superterm/subterm
 * mappings. It can also be used as an embedded component via
 * @AttributeOverrides in entities like ExpressionResultGenerated.
 */
@MappedSuperclass
@Setter
@Getter
public class PostComposedEntity implements Comparable<PostComposedEntity> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superterm_zdb_id")
    @JsonView({View.API.class, View.UI.class})
    protected GenericTerm superterm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subterm_zdb_id")
    @JsonView({View.API.class, View.UI.class})
    protected GenericTerm subterm;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !getClass().isAssignableFrom(o.getClass())) return false;

        PostComposedEntity that = (PostComposedEntity) o;

        if (subterm != null ? !subterm.equals(that.getSubterm()) : that.getSubterm() != null) return false;
        if (superterm != null ? !superterm.equals(that.getSuperterm()) : that.getSuperterm() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = superterm != null ? superterm.hashCode() : 0;
        result = 31 * result + (subterm != null ? subterm.hashCode() : 0);
        return result;
    }

    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        if (superterm != null)
            builder.append(superterm.getTermName());
        if (subterm != null) {
            builder.append(" : ");
            builder.append(subterm.getTermName());
        }
        return builder.toString();
    }

    public int compareTo(PostComposedEntity o) {
        if (this.equals(o))
            return 0;

        if (superterm.compareTo(o.getSuperterm()) == 0) {
            if (o.getSubterm() == null)
                return 1;
            else if (subterm == null)
                return -1;
            else
                return subterm.compareTo(o.getSubterm());
        } else {
            return superterm.compareTo(o.getSuperterm());
        }
    }

    public String toString() {
        if (subterm == null)
            return superterm.getTermName();
        else
            return superterm.getTermName() + " " + subterm.getTermName();
    }

    /**
     * Checks if a given term is found in super or sub term position
     * Note: If the provided term is null this method return false.
     *
     * @param term Term
     * @return true or false
     */
    public boolean contains(GenericTerm term) {
        if (term == null)
            return false;

        if (superterm != null && superterm.equals(term))
            return true;
        if (subterm != null && subterm.equals(term))
            return true;

        return false;
    }
}
