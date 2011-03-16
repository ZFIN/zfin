package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.util.StringUtils;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 */
public class ExpressedTermDTO implements IsSerializable, Comparable<ExpressedTermDTO> {

    protected String zdbID;
    protected TermDTO superterm;
    protected TermDTO subterm;

    private boolean expressionFound;

    public boolean isExpressionFound() {
        return expressionFound;
    }

    public void setExpressionFound(boolean expressionFound) {
        this.expressionFound = expressionFound;
    }

    public String getDisplayName() {
        String composedTerm = superterm.getName();
        if (subterm != null)
            composedTerm += ":" + subterm.getName();
        return composedTerm;
    }

    public TermDTO getSuperterm() {
        return superterm;
    }

    public void setSuperterm(TermDTO superterm) {
        this.superterm = superterm;
    }

    public TermDTO getSubterm() {
        return subterm;
    }

    public void setSubterm(TermDTO subterm) {
        this.subterm = subterm;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressedTermDTO termDTO = (ExpressedTermDTO) o;

        String supertermID = superterm.getZdbID();
        if (supertermID != null ? !supertermID.equals(termDTO.getSuperterm().getZdbID()) : termDTO.getSuperterm().getZdbID() != null)
            return false;

        if (subterm == null && termDTO.getSubterm() == null)
            return true;
        if ((subterm != null && termDTO.getSubterm() == null) ||
                (subterm == null && termDTO.getSubterm() != null))
            return false;

        if (subterm.getZdbID().equals(termDTO.getSubterm().getZdbID()))
            return true;

        return false;
    }

    @Override
    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
    public int hashCode() {
        int result = (superterm.getZdbID() != null ? superterm.getZdbID().hashCode() : 0);
        if (subterm != null)
            result = 31 * result + subterm.getZdbID().hashCode();
        result += expressionFound ? 43 : 13;
        return result;
    }

    public String getUniqueID() {
        String composedID = superterm.getZdbID();
        if (subterm != null)
            composedID += ":" + subterm.getZdbID();
        return composedID;
    }

    public int compareTo(ExpressedTermDTO o) {
        if (o == null)
            return 1;
        // if the superterms are different sort by superterm
        if (!superterm.getName().equals(o.getSuperterm().getName()))
            return superterm.getName().compareToIgnoreCase(o.getSuperterm().getName());

        // if superterms are the same sort by subterm.
        if (subterm == null && o.getSubterm() != null)
            return -1;
        if (subterm != null && o.getSubterm() == null)
            return 1;
        if (subterm == null && o.getSubterm() == null)
            return 0;

        if (subterm.getName().equalsIgnoreCase(o.getSubterm().getName())) {
            if (expressionFound && !o.isExpressionFound())
                return -1;
            if (!expressionFound && o.isExpressionFound())
                return 1;
        }
        return subterm.getName().compareToIgnoreCase(o.getSubterm().getName());
    }

    /**
     * Checks equality based on term names only. This is needed for checking if a
     * new proposed post-composed term already exists.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnly(ExpressedTermDTO expressedTerm) {
        if (!StringUtils.equals(superterm.getName(), expressedTerm.getSuperterm().getName()))
            return false;
        if (subterm == null && expressedTerm.getSubterm() == null)
            return true;
        if ((subterm != null && expressedTerm.getSubterm() == null) ||
                (subterm == null && expressedTerm.getSubterm() != null))
            return false;
        if (!StringUtils.equals(subterm.getName(), expressedTerm.getSubterm().getName()))
            return false;
        return true;
    }
}