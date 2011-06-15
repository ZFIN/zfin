package org.zfin.ontology;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of the Term interface.
 */
public class GenericTerm extends AbstractTerm {

    private transient final Logger logger = Logger.getLogger(GenericTerm.class);

//    @Override
//    public DevelopmentStage getStart() {
////        if(true) throw new RuntimeException("Not yet implemented") ;
//        return start;
//    }
//
//    @Override
//    public void setStart(DevelopmentStage stage) {
////        if(true) throw new RuntimeException("Not yet implemented") ;
//        this.start = stage;
//    }
//
//    @Override
//    public DevelopmentStage getEnd() {
////        if(true) throw new RuntimeException("Not yet implemented") ;
//        return end;
//    }
//
//    @Override
//    public void setEnd(DevelopmentStage stage) {
////        if(true) throw new RuntimeException("Not yet implemented") ;
//        this.end = stage;
//    }

    // map of child terms for a given relationship type string
    private Map<String, List<Term>> childTermMap = new HashMap<String, List<Term>>(3);

    /**
     * Retrieves all terms that are immediate children of this term
     * via a given relationship type
     *
     * @return list of children terms
     */
    @Override
    public List<Term> getChildrenTerms(String relationshipType) {
        if (relationshipType == null)
            return null;

        List<Term> terms = childTermMap.get(relationshipType);
        if (CollectionUtils.isNotEmpty(terms))
            return terms;

        List<Term> childTerms = new ArrayList<Term>();
        for (TermRelationship rel : relationships) {
            Term relatedTerm = rel.getRelatedTerm(this);
            // the null check comes from the AO which has start and end relationship to stage terms which are not yet set
            // upon deserialization of the obo files.
            if (relatedTerm != null && relatedTerm.equals(rel.getTermTwo()))
                if (rel.getType().equals(relationshipType))
                    childTerms.add(relatedTerm);
        }
        childTermMap.put(relationshipType, childTerms);
        return childTerms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof GenericTerm))
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
