package org.zfin.ontology;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Image;
import org.zfin.util.NumberAwareStringComparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Basic implementation of the Term interface.
 */
public class GenericTerm implements Term, Serializable {

    private String ID;
    private String termName;
    private String oboID;
    private Ontology ontology;
    private boolean obsolete;
    private boolean root;
    private boolean secondary;
    private String comment;
    private Set<TermAlias> synonyms;
    private String definition;
    private Set<Image> images;

    // attribute that is populated lazily
    // transient modifier because we do not want to serialize the whole relationship tree
    // (would lead to a StackOverflowError)
    transient private List<TermRelationship> relationships;
    private List<Term> children;
    // These attributes are set during object creation through a service.
    // they are currently not mapped.
    private DevelopmentStage start;
    private DevelopmentStage end;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isSecondary() {
        return secondary;
    }

    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<TermAlias> getAliases() {
        return synonyms;
    }

    public void setAliases(Set<TermAlias> synonyms) {
        this.synonyms = synonyms;
    }

    public boolean isAliasesExist() {
        return (synonyms != null && !synonyms.isEmpty());
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<TermRelationship> getRelatedTerms() {
        return relationships;
    }

    @Override
    public void setRelatedTerms(List<TermRelationship> relationships) {
        this.relationships = relationships;
    }

    public List<Term> getChildrenTerms() {
        if (CollectionUtils.isNotEmpty(children))
            return children;

        if (relationships == null)
            return null;

        children = new ArrayList<Term>();
        for (TermRelationship rel : relationships) {
            Term relatedTerm = rel.getRelatedTerm(this);
            // the null check comes from the AO which has start and end relationship to stage terms which are not yet set
            // upon deserialization of the obo files.
            if (relatedTerm != null && relatedTerm.equals(rel.getTermTwo()))
                children.add(relatedTerm);
        }
        return children;
    }

    @Override
    public DevelopmentStage getStart() {
        return start;
    }

    @Override
    public void setStart(DevelopmentStage stage) {
        this.start = stage;
    }

    @Override
    public DevelopmentStage getEnd() {
        return end;
    }

    @Override
    public void setEnd(DevelopmentStage stage) {
        this.end = stage;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;

        GenericTerm genericTerm = (GenericTerm) o;

        if (ID != null && genericTerm.getID() != null) {
            return ID.equals(genericTerm.getID());
        }
        if (termName != null ? !termName.equals(genericTerm.getTermName()) : genericTerm.getTermName() != null)
            return false;
        if (oboID != null ? !oboID.equals(genericTerm.oboID) : genericTerm.oboID != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ID != null ? ID.hashCode() : 0;
        result = 31 * result + (termName != null ? termName.hashCode() : 0);
        result = 31 * result + (oboID != null ? oboID.hashCode() : 0);
        return result;
    }

    public int compareTo(Term compTerm) {
        if (compTerm == null)
            return -1;
        NumberAwareStringComparator comparator = new NumberAwareStringComparator();
        return comparator.compare(termName, compTerm.getTermName());
    }
}
