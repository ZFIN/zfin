package org.zfin.anatomy;

import org.zfin.repository.RepositoryFactory;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 * This business object defines an anatomical structure, aka anatomy item or anatomy term.
 *
 *
 * ToDo: This class needs to be refactored to allow for a common interface for a term in any ontology.
 */
public class AnatomyItem implements Serializable {

    public static final String UNKNOWN = "unknown";
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

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
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
}
