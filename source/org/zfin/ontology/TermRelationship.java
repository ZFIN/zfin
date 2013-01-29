package org.zfin.ontology;

import org.zfin.gwt.root.dto.RelationshipType;

import java.io.Serializable;

/**
 */
public interface TermRelationship extends Serializable, Comparable<TermRelationship> {

    public String getZdbId();

    public GenericTerm getTermOne();

    public void setTermOne(GenericTerm term);

    public Term getTermTwo();

    public void setTermTwo(GenericTerm term);

    public GenericTerm getRelatedTerm(GenericTerm t);

    public String getType();

    public void setType(String type);

    public RelationshipType getRelationshipType();


}
