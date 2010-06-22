package org.zfin.anatomy;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.*;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This business object defines an anatomical structure, aka anatomy item or anatomy term.
 * <p/>
 * <p/>
 * ToDo: This class is replaced by the Term / GenericTerm class. Do not use it unless you need the
 * information about start and end stage info to join in a table. The stage info on the GenericTerm class
 * is not populated through a Hibernate Mapping since the stages are terms on the ao term (TERM table) but
 * do not contain all the info that the STAGE table contains.
 */
public class AnatomyItem implements Term {

    private long itemID;
    private String zdbID;
    private String name;
    private String oboID;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private String lowerCaseName;
    private String definition;
    private String description;
    private boolean obsolete;
    private boolean cellTerm;
    // used to do the order by
    private String nameOrder;
    private List<AnatomyRelationship> relatedItems;
    private Set<AnatomySynonym> synonyms;

    private List<TermRelationship> relationships;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public String getNameEscaped() {
        return getName().replaceAll("'", "\\\\'");
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getItemID() {
        return itemID;
    }

    public void setItemID(long itemID) {
        this.itemID = itemID;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOntology(Ontology ontology) {
        //ignore for now
    }

    public Ontology getOntology() {
        return Ontology.ANATOMY;
    }

    public String getComment() {
        return null;
    }

    public void setComment(String comment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public String getDefinition() {
        return StringUtils.isEmpty(definition) ? null : definition;
    }

    public String getFormattedDefinition() {
        if (StringUtils.isEmpty(definition)) {
            return null;
        } else {
            return definition.replaceAll("a href", "a target=\\\"_blank\\\" class=\\\"external\\\" href");
        }
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public boolean isRoot() {
        return false;
    }

    public void setRoot(boolean root) {
        // Todo when Ao goes into term table
    }

    public boolean isSecondary() {
        return false;
    }

    public void setSecondary(boolean secondary) {
        // Todo when Ao goes into term table
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public List<AnatomyRelationship> getRelatedItems() {
        return relatedItems;
    }

    public String getLowerCaseName() {
        return lowerCaseName;
    }

    public void setLowerCaseName(String lowerCaseName) {
        this.lowerCaseName = lowerCaseName;
    }

    public Set<AnatomySynonym> getSynonyms() {
        return (synonyms == null || synonyms.size() == 0) ? null : synonyms;
    }

    public Set<TermAlias> getAliases() {
        return null;
    }

    public void setAliases(Set<TermAlias> aliases) {
        // Todo when Ao goes into term table
    }

    public void setSynonyms(Set<AnatomySynonym> synonyms) {
        this.synonyms = synonyms;
    }

    public void setRelatedItems(List<AnatomyRelationship> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public List<AnatomyRelationship> getAnatomyRelations() {
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        List<AnatomyRelationship> relations = ar.getAnatomyRelationships(this);
        return relations;
    }


    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

    public String getID() {
        return zdbID;
    }

    public void setID(String id) {
        // Todo when Ao goes into term table
    }

    public String getTermName() {
        return name;
    }

    public void setTermName(String termName) {
        // Todo when Ao goes into term table
    }

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String NEWLINE_PLUS_INDENT = NEWLINE + "    ";

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Anatomy Item [BO]");
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("zdbID: ").append(zdbID);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("oboID: ").append(oboID);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("name: ").append(name);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Obsolete: ").append(obsolete);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Definition: ").append(definition);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("Description: ").append(description);
        sb.append(NEWLINE);
        sb.append("Start Stage: ").append(start);
        sb.append(NEWLINE);
        sb.append("End Stage: ").append(end);
        sb.append(NEWLINE);
        return sb.toString();
    }

    public boolean isCellTerm() {
        return cellTerm;
    }

    public void setCellTerm(boolean cellTerm) {
        this.cellTerm = cellTerm;
    }

    public List<TermRelationship> getRelatedTerms() {
        if (relationships != null)
            return relationships;

        relationships = new ArrayList<TermRelationship>();
        OntologyRepository ontologyOntologyRepository = RepositoryFactory.getOntologyRepository();
        relationships.addAll(ontologyOntologyRepository.getTermRelationships(this));
        return relationships;
    }

    public int compareTo(Term term) {
        if (term == null)
            return +1;
        if (term instanceof AnatomyItem)
            return nameOrder.compareTo(((AnatomyItem)term).getNameOrder());
        return +1;
    }

    public boolean equals(AnatomyItem anotherAO) {
        return anotherAO.getZdbID().equalsIgnoreCase(zdbID);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnatomyItem))
            return false;
        AnatomyItem term = (AnatomyItem) o;
        return
                cellTerm == term.isCellTerm() &&
                        obsolete == term.isObsolete() &&
                        StringUtils.equals(definition, term.getDefinition()) &&
                        StringUtils.equals(description, term.getDescription()) &&
                        StringUtils.equals(name, term.getName()) &&
                        StringUtils.equals(nameOrder, term.getNameOrder()) &&
                        StringUtils.equals(oboID, term.getOboID()) &&
                        StringUtils.equals(zdbID, term.getZdbID()) &&
                        ObjectUtils.equals(start, term.getStart()) &&
                        ObjectUtils.equals(end, term.getEnd());
    }

    @Override
    public int hashCode() {
        int hash = 37;
        if (definition != null)
            hash = hash * definition.hashCode();
        if (description != null)
            hash += hash * description.hashCode();
        if (name != null)
            hash += hash * name.hashCode();
        if (nameOrder != null)
            hash += hash * nameOrder.hashCode();
        if (oboID != null)
            hash += hash * oboID.hashCode();
        if (zdbID != null)
            hash += hash * zdbID.hashCode();
        if (start != null)
            hash += hash * start.hashCode();
        if (end != null)
            hash += hash * end.hashCode();
        return hash;
    }

    /**
     * Retrieves all terms that are immediate children of this term.
     *
     * @return list of children terms
     */
    public List<Term> getChildrenTerms() {
        // ToDo: To be implemented
        throw new RuntimeException("Not yet implemented");
    }
}
