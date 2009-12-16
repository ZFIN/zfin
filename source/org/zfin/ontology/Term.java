package org.zfin.ontology;

import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public interface Term {

    String getID();

    String getTermName();

    String getOboID();

    String getOntologyName();

    String getComment();

    boolean isObsolete();

    boolean isRoot();

    boolean isSecondary();

    Set<TermAlias> getSynonyms();

}
