package org.zfin.ontology;

import org.zfin.gwt.root.dto.RelationshipType;

import java.io.Serializable;

/**
 */
public interface TermRelationship extends Serializable{

    public String getZdbId();
    public Term getTermOne();
    public Term getTermTwo();
    public Term getRelatedTerm(Term t);
    public String getType();
    public RelationshipType getRelationshipType();


}
