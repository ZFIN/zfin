package org.zfin.ontology;

import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface Term extends OntologyTerm{

    String getID();

    String getOboID();

    String getOntologyName();

    String getComment();

    boolean isRoot();

    boolean isSecondary();

    Set<TermAlias> getSynonyms();

}
