package org.zfin.ontology;

import org.apache.log4j.Logger;

/**
 * Basic implementation of the Term interface.
 */
public class GenericTerm extends AbstractTerm {

    private transient final Logger logger = Logger.getLogger(GenericTerm.class);

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        GenericTerm genericTerm = (GenericTerm) o;

        if (zdbID != null && genericTerm.getZdbID() != null) {
            return zdbID.equals(genericTerm.getZdbID());
        }
        if (termName != null ? !termName.equals(genericTerm.getTermName()) : genericTerm.getTermName() != null){
            return false;
		}
        if (oboID != null ? !oboID.equals(genericTerm.getOboID()) : genericTerm.getOboID() != null){
            return false;
		}

        return true;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GenericTerm").append('\'');
        sb.append(super.toString());
        return sb.toString();
    }

}
