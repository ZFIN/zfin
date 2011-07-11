package org.zfin.ontology;

/**
 * A class to group superterm and subterm into a single object,
 * intended to be used as a component in expression and phenotype
 * records
 */
public class PostComposedEntity implements Comparable<PostComposedEntity> {

    private GenericTerm superterm;
    private GenericTerm subterm;

    public GenericTerm getSuperterm() {
        return superterm;
    }

    public void setSuperterm(GenericTerm superterm) {
        this.superterm = superterm;
    }

    public GenericTerm getSubterm() {
        return subterm;
    }

    public void setSubterm(GenericTerm subterm) {
        this.subterm = subterm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
