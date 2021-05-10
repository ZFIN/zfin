package org.zfin.ontology;

import org.zfin.gwt.root.dto.RelationshipType;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

public interface TermRelationship extends Serializable, Comparable<TermRelationship> {

    String getZdbId();

    GenericTerm getTermOne();

    void setTermOne(GenericTerm term);

    Term getTermTwo();

    void setTermTwo(GenericTerm term);

    GenericTerm getRelatedTerm(GenericTerm t);

    String getType();

    void setType(String type);

    RelationshipType getRelationshipType();


}
