package org.zfin.ontology;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Image;
import org.zfin.sequence.ForeignDB;
import org.zfin.util.NumberAwareStringComparator;

import java.util.*;

/**
 * Basic implementation of the Term interface.
 * This is just a convenience class as GenericTerm do not share table data.
 */
public abstract class AbstractTerm implements Term {

    protected String zdbID;
    protected String termName;
    protected String termNameOrder;
    protected String oboID;
    protected Ontology ontology;
    protected boolean obsolete;
    protected boolean root;
    protected boolean secondary;
    protected String comment;
    protected Set<TermAlias> synonyms;
    protected String definition;


    protected Set<Image> images;
    private Set<Subset> subsets;
    private Set<TermDefinitionReference> definitionReferences;
    private Set<TermExternalReference> externalReferences;

    protected Set<TermRelationship> childTermRelationships;
    protected Set<TermRelationship> parentTermRelationships;

    // attribute that is populated lazily
    // transient modifier because we do not want to serialize the whole relationship tree
    // (would lead to a StackOverflowError)
    transient protected List<TermRelationship> relationships;
//    protected List<Term> children;

    // These attributes are set during object creation through a service.
    // they are currently not mapped.

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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

    public SortedSet getSortedAliases() {
        return new TreeSet<TermAlias>(synonyms);
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


    public Set<TermRelationship> getChildTermRelationships() {
        return childTermRelationships;
    }

    public void setChildTermRelationships(Set<TermRelationship> childTermRelationships) {
        this.childTermRelationships = childTermRelationships;
    }

    public Set<TermRelationship> getParentTermRelationships() {
        return parentTermRelationships;
    }

    public void setParentTermRelationships(Set<TermRelationship> parentTermRelationships) {
        this.parentTermRelationships = parentTermRelationships;
    }

    public String getTermNameOrder() {
        return termNameOrder;
    }

    public void setTermNameOrder(String termNameOrder) {
        this.termNameOrder = termNameOrder;
    }

    @Override
    public Set<Term> getChildTerms() {
        Set<Term> terms = new HashSet<Term>();
        for (TermRelationship termRelationship : getChildTermRelationships()) {
            terms.add(termRelationship.getTermTwo());
        }
        return terms;
    }

    @Override
    public Set<Term> getParentTerms() {
        Set<Term> terms = new HashSet<Term>();
        for (TermRelationship termRelationship : getParentTermRelationships()) {
            terms.add(termRelationship.getTermOne());
        }
        return terms;
    }

    public List<TermRelationship> getAllDirectlyRelatedTerms() {
        List<TermRelationship> terms = new ArrayList<TermRelationship>();
        Set<TermRelationship> childTermRelationships = getChildTermRelationships();
        if (childTermRelationships != null)
            terms.addAll(childTermRelationships);
        Set<TermRelationship> parentTermRelationships = getParentTermRelationships();
        if (parentTermRelationships != null)
            terms.addAll(parentTermRelationships);

        return terms;
//        if (relationships != null){
//            return relationships;
//        }
//
//        relationships = new ArrayList<TermRelationship>();
//        relationships.addAll(RepositoryFactory.getOntologyRepository().getTermRelationships(this));
////        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository() ;
////        Term t = ontologyRepository.getTermByOboID(getOboID());
////        relationships.addAll(ontologyRepository.getTermRelationships(t));
//        return relationships;
    }

//    public List<TermRelationship> getRelatedTerms() {
//        return relationships;
//    }
//
//    @Override
//    public void setRelatedTerms(List<TermRelationship> relationships) {
//        this.relationships = relationships;
//    }

//    public List<Term> getChildrenTerms() {
//        if (CollectionUtils.isNotEmpty(children))
//            return children;
//
//        if (relationships == null)
//            return null;
//
//        children = new ArrayList<Term>();
//        for (TermRelationship rel : relationships) {
//            Term relatedTerm = rel.getRelatedTerm(this);
//            // the null check comes from the AO which has start and end relationship to stage terms which are not yet set
//            // upon deserialization of the obo files.
//
//            // is a hibernate object and so expects to have a session
//            // so need to explicitly be bound to it since we don't know how this object was retrieved (memory vs DB).
//            if (relatedTerm != null && relatedTerm.equals(rel.getTermTwo()))
//                children.add(relatedTerm);
//        }
//        return children;
//    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    @Override
    public Set<Subset> getSubsets() {
        return subsets;
    }

    @Override
    public void setSubsets(Set<Subset> subsets) {
        this.subsets = subsets;
    }

    @Override
    public boolean isPartOfSubset(Subset subset) {
        return !(this.subsets == null || subset == null) && subsets.contains(subset);
    }

    @Override
    public boolean isPartOfSubset(String subsetName) {
        if (subsets == null)
            return false;
        for (Subset subset : subsets) {
            if (subset.getInternalName().equals(subsetName.toLowerCase().trim()))
                return true;
        }
        return false;
    }


    public Set<TermDefinitionReference> getDefinitionReferences() {
        return definitionReferences;
    }

    public void setDefinitionReferences(Set<TermDefinitionReference> definitionReferences) {
        this.definitionReferences = definitionReferences;
    }

    public Set<TermExternalReference> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(Set<TermExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }
    public String getReferenceLink() {
        if (checkIfSingleReference()) return null;
        return getReferenceLink(definitionReferences.iterator().next());
    }

    public static String getReferenceLink(TermDefinitionReference reference) {
        return reference.getForeignDB().getDbUrlPrefix() + reference.getReference();
    }

    private boolean checkIfSingleReference() {
        if (definitionReferences == null)
            return true;
        if (definitionReferences.size() != 1)
            throw new RuntimeException("Only one reference is supported for this method.");
        return false;
    }

    private DevelopmentStage start;
    private DevelopmentStage end;

    private String startZdbID;
    private String endZdbID;

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


    public String getStartZdbID() {
        return startZdbID;
    }

    public void setStartZdbID(String startZdbID) {
        this.startZdbID = startZdbID;
    }

    public String getEndZdbID() {
        return endZdbID;
    }

    public void setEndZdbID(String endZdbID) {
        this.endZdbID = endZdbID;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
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


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{ID='").append(zdbID).append('\'');
        sb.append(", termName='").append(termName).append('\'');
        sb.append(", oboID='").append(oboID).append('\'');
        sb.append(", ontology=").append(ontology);
        sb.append(", obsolete=").append(obsolete);
        sb.append(", root=").append(root);
        sb.append(", secondary=").append(secondary);
        sb.append('}');
        return sb.toString();
    }
}
