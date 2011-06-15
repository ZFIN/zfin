package org.zfin.gwt.root.dto;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.Subset;

import java.io.Serializable;
import java.util.*;

/**
 * Data Transfer Object for a composed term with expressed-in boolean.
 *
 * http://java.sun.com/developer/technicalArticles/ALT/serialization/
 */
public class TermDTO extends RelatedEntityDTO implements Serializable {

    private static final long serialVersionUID = 955354076929860754L;

    private String oboID;
    private String definition;
    private String comment;
    private boolean obsolete;
    private OntologyDTO ontology;
    private Set<TermDTO> childrenTerms; // should be a unique set
    private Set<TermDTO> parentTerms; // should be a unique set
    //    private List<AliasDTO> aliases;
    private Set<String> aliases; // should be a unique set
    private StageDTO startStage ;
    private StageDTO endStage ;
    private Set<String> subsets;

    private String relationshipType ; // in the case that this is just a Term in a relationship
    private int significance; // when used as an alias
    //    private Map<String, List<TermInfoDTO>> relatedTermInfos = new TreeMap<String, List<TermInfoDTO>>(new RelationshipComparatorDTO());
    private Map<String,List<TermDTO>> relatedTermMap = null ;

    public String getTermName() {
        return name;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public OntologyDTO getOntology() {
        return ontology;
    }

    public void setOntology(OntologyDTO ontology) {
        this.ontology = ontology;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Set<String> getSubsets() {
        return subsets;
    }

    public void setSubsets(Set<String> subsets) {
        this.subsets = subsets;
    }

    public boolean isSubsetOf(String subsetName) {
        if (subsets == null)
            return false;
        return subsets.contains(subsetName);
    }

    public boolean isSubsetOf(SubsetDTO subset) {
        if (subsets == null)
            return false;
        return isSubsetOf(subset.toString());
    }

    public boolean equalsByName(TermDTO term) {
        if (term == null)
            return false;
        if(!ontology.equals(term.getOntology()))
            return false;
        return StringUtils.equalsWithNullString(name, term.getTermName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermDTO termDTO = (TermDTO) o;

        if (zdbID != null ? !zdbID.equals(termDTO.zdbID) : termDTO.zdbID != null) return false;
        if (name != null ? !name.equals(termDTO.name) : termDTO.name != null) return false;

        return true;
    }

//    @Override
//    @SuppressWarnings({"NonFinalFieldReferencedInHashCode", "SuppressionAnnotation"})
//    public int hashCode() {
//        return 31 + (zdbID != null ? zdbID.hashCode() : 0);
//    }


    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (getName()!= null ? getName().hashCode() : 0);
//        result = 31 * result + (comment != null ? comment.hashCode() : 0);
//        result = 31 * result + (obsolete ? 1 : 0);
//        result = 31 * result + (ontology != null ? ontology.hashCode() : 0);
//        result = 31 * result + (childrenTerms != null ? childrenTerms.hashCode() : 0);
//        result = 31 * result + (parentTerms != null ? parentTerms.hashCode() : 0);
//        result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
//        result = 31 * result + (startStage != null ? startStage.hashCode() : 0);
//        result = 31 * result + (endStage != null ? endStage.hashCode() : 0);
//        result = 31 * result + (relationshipType != null ? relationshipType.hashCode() : 0);
//        result = 31 * result + significance;
        return result;
    }


//    @Override
//    public int compareTo(Object o) {
//        if (o == null){
//            return 1;
//        }
//        if (!(o instanceof TermInfoDTO)){
//            return 1;
//        }
//        TermInfoDTO termInfoDTO = (TermInfoDTO) o ;
//        NumberAwareStringComparatorDTO comparator = new NumberAwareStringComparatorDTO();
//        return comparator.compare(getName(), termInfoDTO.getName());
//    }

    /**
     * Checks equality based on term name and Ontology only. This is needed for checking if a
     * new proposed post-composed term already exists.
     * The IDs are not known in our current auto-complete but only the names so we cannot check by ID.
     *
     * @param expressedTerm expressed Term
     * @return true or false
     */
    public boolean equalsByNameOnlyAndOntology(TermDTO expressedTerm) {
        if (!StringUtils.equals(name, expressedTerm.getName()))
            return false;
        if (ontology != expressedTerm.getOntology())
            return false;
        return true;
    }

    public Set<TermDTO> getChildrenTerms() {
        return childrenTerms;
    }

    public void setChildrenTerms(Set<TermDTO> childrenTerms) {
        this.childrenTerms = childrenTerms;
    }

    public Set<TermDTO> getParentTerms() {
        return parentTerms;
    }

    public void setParentTerms(Set<TermDTO> parentTerms) {
        this.parentTerms = parentTerms;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    //    public List<AliasDTO> getAliases() {
//        return aliases;
//    }
//
//    public void setAliases(List<AliasDTO> aliases) {
//        this.aliases = aliases;
//    }

    public StageDTO getStartStage() {
        return startStage;
    }

    public void setStartStage(StageDTO startStage) {
        this.startStage = startStage;
    }

    public StageDTO getEndStage() {
        return endStage;
    }

    public void setEndStage(StageDTO endStage) {
        this.endStage = endStage;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public int getSignificance() {
        return significance;
    }

    public void setSignificance(int significance) {
        this.significance = significance;
    }

    public Map<String, Set<TermDTO>> getAllRelatedTerms() {
        Map<String,Set<TermDTO>> relatedTermMap = new TreeMap<String,Set<TermDTO>>() ;

        if(childrenTerms!=null){
            for(TermDTO term  : childrenTerms){
                String display = RelationshipType.getInverseRelationshipByName(term.getRelationshipType()).getDisplay();
                Set<TermDTO> termDTOs = relatedTermMap.get(display);
                if(termDTOs==null){
                    termDTOs = new TreeSet<TermDTO>() ;
                    relatedTermMap.put(display,termDTOs);
                }
                termDTOs.add(term) ;
            }
        }
        if(parentTerms!=null){
            for(TermDTO term  : parentTerms){
                String display = RelationshipType.getRelationshipTypeByDbName(term.getRelationshipType()).getDisplay();
                Set<TermDTO> termDTOs = relatedTermMap.get(display);
                if(termDTOs==null){
                    termDTOs = new TreeSet<TermDTO>() ;
                    relatedTermMap.put(display, termDTOs);
                }
                termDTOs.add(term) ;
            }
        }

        return relatedTermMap ;
    }

    public boolean isAliasesExist() {
        return aliases!=null && aliases.size()>0 ;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TermDTO");
        sb.append(super.toString()).append("\n");
        sb.append("{oboID='").append(oboID).append('\'');
        sb.append(", definition='").append(definition).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", obsolete=").append(obsolete);
        sb.append(", ontology=").append(ontology);
        sb.append(", childrenTerms=").append(childrenTerms);
        sb.append(", parentTerms=").append(parentTerms);
        sb.append(", aliases=").append(aliases);
        sb.append(", startStage=").append(startStage);
        sb.append(", endStage=").append(endStage);
        sb.append(", relationshipType='").append(relationshipType).append('\'');
        sb.append(", significance=").append(significance);
        sb.append('}');
        return sb.toString();
    }



    public void shallowCopyFrom(TermDTO termDTO) {
        setZdbID(termDTO.getZdbID());
        setName(termDTO.getName());
        setAliases(termDTO.getAliases());
        setComment(termDTO.getComment());
        setDefinition(termDTO.getDefinition());
        setEndStage(termDTO.getEndStage());
        setStartStage(termDTO.getStartStage());
        setOboID(termDTO.getOboID());
        setOntology(termDTO.getOntology());
        // we explicitly don't copy this
//        setRelationshipType(termDTO.getRelationshipType());
        setLink(termDTO.getLink());
        setObsolete(termDTO.isObsolete());

    }

//    @Override
//    public int compareTo(Object object) {
//        if(object instanceof TermDTO){
//            return comparator.compare(getName(),((TermDTO) object).getName());
//        }
//        else{
//            return comparator.compare(getName(),object.toString()) ;
//        }
//    }

    @Override
    public int compareTo(Object o) {
        if (o == null)
            return 1;
        if (!(o instanceof TermDTO))
            return 1;
        TermDTO term = (TermDTO) o;
//        return comparator.compare(getName(),term.getName());
        return getName().compareTo(term.getName());

    }

    public void addSubset(SubsetDTO relationalSlim) {
        if (subsets == null)
            subsets = new HashSet<String>(1);
        subsets.add(relationalSlim.toString());
    }

    public boolean isPartOfSubset(SubsetDTO subset) {
        if (subsets == null)
            return false;
        for (String subsetNameStr : subsets) {
            if (subsetNameStr.equals(subset.toString()))
                return true;
        }
        return false;
    }

    public List<TermDTO> getChildrenTerms(String relationshipType) {
       return null;
    }

    public static final String RELATIONAL__SLIM = "relational_slim";

    public boolean isRelatedTerm() {
        if (subsets == null)
            return false;
        for (String subsetName : subsets) {
            if (subsetName.equals(RELATIONAL__SLIM))
                return true;
        }
        return false;
    }
}