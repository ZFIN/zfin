package org.zfin.ontology;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Image;
import org.zfin.util.NumberAwareStringComparator;

import javax.persistence.*;
import java.util.*;

/**
 * Basic implementation of the Term interface.
 */
@Entity
@Table(name = "term")
public class GenericTerm implements Term<GenericTermRelationship> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "TERM")
            })
    @Column(name = "term_zdb_id")
    protected String zdbID;
    @Column(name = "term_name")
    protected String termName;
    @Column(name = "term_name_order")
    protected String termNameOrder;
    @Column(name = "term_ont_id")
    protected String oboID;
    @Column(name = "term_ontology")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.ontology.Ontology")})
    protected Ontology ontology;
    @Column(name = "term_is_obsolete")
    protected boolean obsolete;
    @Column(name = "term_is_root")
    protected boolean root;
    @Column(name = "term_is_secondary")
    protected boolean secondary;
    @Column(name = "term_comment")
    protected String comment;
    @OneToMany(mappedBy = "term", fetch = FetchType.LAZY)
    protected Set<TermAlias> synonyms;
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "term_definition")
    protected String definition;
    @ManyToMany
    @JoinTable(name = "int_image_term", joinColumns = {
            @JoinColumn(name = "iit_term_zdb_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "iit_img_zdb_id",
                    nullable = false, updatable = false)})
    protected Set<Image> images;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_subset", joinColumns = {
            @JoinColumn(name = "termsub_term_zdb_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "termsub_subset_id",
                    nullable = false, updatable = false)})
    private Set<Subset> subsets;
    @OneToMany(mappedBy = "term")
    private Set<TermDefinitionReference> definitionReferences;
    @OneToMany(mappedBy = "term")
    private Set<TermExternalReference> externalReferences;
    @OneToMany(mappedBy = "termOne")
    protected Set<GenericTermRelationship> childTermRelationships;
    @OneToMany(mappedBy = "termTwo")
    protected Set<GenericTermRelationship> parentTermRelationships;
    //Note: For some reason Hibernate does not do well with term_stage used for start and end stages.
    // It only runs one query, typically retrieves only start and leaves end null.. Feels like bug..
    // it worked in xml mapping. Thus, mapping term_stage as a n ew entity and retrieve start and end
    // from there.
    //
      /*
        @OneToOne(optional = true)
        @JoinTable(name = "term_stage",
                joinColumns = {
                        @JoinColumn(name = "ts_term_zdb_id", unique = true)
                },
                inverseJoinColumns = {
                        @JoinColumn(name = "ts_end_stg_zdb_id")
                }
        )
    protected DevelopmentStage end;
    */
    /*
        @OneToOne(optional = true)
        @JoinTable(name = "term_stage",
                joinColumns = {
                        @JoinColumn(name = "ts_term_zdb_id", unique = true)
                },
                inverseJoinColumns = {
                        @JoinColumn(name = "ts_start_stg_zdb_id")
                }
        )
    protected DevelopmentStage start;
    */

    @OneToOne(mappedBy = "term", fetch = FetchType.LAZY)
    @NotFound(action= NotFoundAction.IGNORE)
    protected TermStage termStage;
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

    public TreeSet getSortedAliases() {
        return new TreeSet<>(synonyms);
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


    public Set<GenericTermRelationship> getChildTermRelationships() {
        return childTermRelationships;
    }

    public void setChildTermRelationships(Set<GenericTermRelationship> childTermRelationships) {
        this.childTermRelationships = childTermRelationships;
    }

    public Set<GenericTermRelationship> getParentTermRelationships() {
        return parentTermRelationships;
    }

    public void setParentTermRelationships(Set<GenericTermRelationship> parentTermRelationships) {
        this.parentTermRelationships = parentTermRelationships;
    }

    public String getTermNameOrder() {
        return termNameOrder;
    }

    public void setTermNameOrder(String termNameOrder) {
        this.termNameOrder = termNameOrder;
    }

    @Override
    public Set<GenericTerm> getChildTerms() {
        Set<GenericTerm> terms = new HashSet<>();
        for (GenericTermRelationship termRelationship : getChildTermRelationships()) {
            terms.add(termRelationship.getTermTwo());
        }
        return terms;
    }

    @Override
    public Set<GenericTerm> getParentTerms() {
        Set<GenericTerm> terms = new HashSet<>();
        for (TermRelationship termRelationship : getParentTermRelationships()) {
            terms.add(termRelationship.getTermOne());
        }
        return terms;
    }

    public List<GenericTermRelationship> getAllDirectlyRelatedTerms() {
        List<GenericTermRelationship> terms = new ArrayList<>();
        Set<GenericTermRelationship> childTermRelationships = getChildTermRelationships();
        if (childTermRelationships != null)
            terms.addAll(childTermRelationships);
        Set<GenericTermRelationship> parentTermRelationships = getParentTermRelationships();
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

    public SortedSet<TermExternalReference> getSortedXrefs() {
        return new TreeSet<>(externalReferences);
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

    @Override
    public DevelopmentStage getStart() {
        if (termStage == null)
            return null;
        return termStage.getStart();
    }

    @Override
    public DevelopmentStage getEnd() {
        if (termStage == null)
            return null;
        return termStage.getEnd();

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

    // map of child terms for a given relationship type string

    @Transient
    private Map<String, List<Term>> childTermMap = new HashMap<>(3);

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

        List<Term> childTerms = new ArrayList<>();
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
        if (termName != null ? !termName.equals(genericTerm.getTermName()) : genericTerm.getTermName() != null) {
            return false;
        }
        if (oboID != null ? !oboID.equals(genericTerm.getOboID()) : genericTerm.getOboID() != null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean useForAnnotations() {
        for (Subset subset : getSubsets()) {
            if (subset.getInternalName().equals(Subset.GO_CHECK_DO_NOT_USE_FOR_ANNOTATIONS) ||
                    subset.getInternalName().equals(Subset.GO_CHECK_DO_NOT_USE_FOR_MANUAL_ANNOTATIONS))
                return false;
        }
        return true;
    }


    public Set<GenericTerm> getAllChildren() {
        if (getChildTerms() == null)
            return null;
        Set<GenericTerm> childSet = new HashSet<>();
        for (GenericTerm child : getChildTerms()) {
            childSet.add(child);
            Set<GenericTerm> allChildren = child.getAllChildren();
            if (allChildren != null)
                childSet.addAll(allChildren);
        }
        return childSet;
    }
}
