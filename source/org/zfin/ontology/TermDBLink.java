package org.zfin.ontology;

import org.zfin.ontology.Term;
import org.zfin.ontology.GenericTerm;
import org.zfin.sequence.DBLink;
import org.zfin.ontology.TermDBLink;

import java.io.Serializable;

/**
 */
public class TermDBLink  extends DBLink implements Comparable<TermDBLink>, Serializable {

  




        // Logger logger = Logger.getLogger(TermDBLink.class);

        private GenericTerm term;

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }
//    private Accession referencingAccession ;



        public boolean equals(Object o) {
            if (o instanceof org.zfin.ontology.TermDBLink) {
                org.zfin.ontology.TermDBLink dbLink = (org.zfin.ontology.TermDBLink) o;
//            if( getZdbID()!=null && dbLink.getZdbID().equals(getZdbID()) ){
//                return true ;
//            }

                if (dbLink.getTerm().getZdbID().equals(dbLink.getTerm().getZdbID())
                        &&
                        dbLink.getAccessionNumber().equals(dbLink.getAccessionNumber())
                        &&
                        dbLink.getReferenceDatabase().equals(dbLink.getReferenceDatabase())
                        ) {
                    return true;
                }
            }
            return false;
        }


        public int hashCode() {
            int result = 1;
//        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
            result += (getTerm() != null ? getTerm().hashCode() : 0) * 13;
            result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
            result += (getReferenceDatabase() != null ? getReferenceDatabase().getZdbID().hashCode() : 0) * 17;
            return result;
        }


        public String toString() {
            String returnString = "";
            returnString += getZdbID() + "\n";
            returnString += getAccessionNumber() + "\n";
            returnString += getLength() + "\n";
            returnString += getReferenceDatabase().getZdbID() + "\n";
            returnString += getTerm().getZdbID() + "\n";
            returnString += getTerm().getTermName() + "\n";
            return returnString;
        }

        /**
         * Sort by accessionNBumber, reference DB id, and finally feature name
         *
         * @param termDBLink termDBLink to compare to.
         * @return Returns java comparison
         */
        public int compareTo(org.zfin.ontology.TermDBLink termDBLink) {

            int accCompare = getAccessionNumber().compareTo(termDBLink.getAccessionNumber());
            if (accCompare != 0) {
                return accCompare;
            }

            int refDBCompare = getReferenceDatabase().getZdbID().compareTo(termDBLink.getReferenceDatabase().getZdbID());
            if (refDBCompare != 0) {
                return refDBCompare;
            }

            int termCompare = getTerm().getZdbID().compareTo(termDBLink.getTerm().getZdbID());
            if (termCompare != 0) {
                return termCompare;
            }

            return 0;
        }

    }
