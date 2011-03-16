package org.zfin.anatomy;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.zfin.ontology.*;

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
public class AnatomyItem extends AbstractTerm {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String NEWLINE_PLUS_INDENT = NEWLINE + "    ";

    private String lowerCaseName;
    private String description;
    private boolean cellTerm;
    // used to do the order by
    private String nameOrder;
    private List<AnatomyRelationship> relatedItems;
    private Set<AnatomySynonym> synonyms;

    protected DevelopmentStage start;
    protected DevelopmentStage end;

    public String getNameEscaped() {
        return getTermName().replaceAll("'", "\\\\'");
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
//        return (synonyms == null || synonyms.size() == 0) ? null : synonyms;
        return synonyms;
    }

    public Set<TermAlias> getAliases() {
        return null;
    }

    public void setAliases(Set<TermAlias> aliases) {
        // Todo when Ao goes into term table
    }

    public boolean isAliasesExist() {
        return false;
    }

    public void setSynonyms(Set<AnatomySynonym> synonyms) {
        this.synonyms = synonyms;
    }

    public void setRelatedItems(List<AnatomyRelationship> relatedItems) {
        this.relatedItems = relatedItems;
    }

    public String getNameOrder() {
        return nameOrder;
    }

    public void setNameOrder(String nameOrder) {
        this.nameOrder = nameOrder;
    }

//    public void setZdbID(String id) {
//        // Todo when Ao goes into term table
//    }
//
//    public void setTermName(String termName) {
//        // Todo when Ao goes into term table
//    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage stage) {
        this.start = stage;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage stage) {
        this.end = stage;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Anatomy Item [BO]");
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("zdbID: ").append(zdbID);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("oboID: ").append(oboID);
        sb.append(NEWLINE_PLUS_INDENT);
        sb.append("name: ").append(termName);
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


//    @Override
//    public void setRelatedTerms(List<TermRelationship> relationships) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }

    public int compareTo(Term term) {
        if (term == null)
            return +1;
        if (term instanceof AnatomyItem)
            return nameOrder.compareTo(((AnatomyItem) term).getNameOrder());
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
                        StringUtils.equals(termName, term.getTermName()) &&
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
        if (termName != null)
            hash += hash * termName.hashCode();
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
    public Set<TermRelationship> getChildTermRelationships() {
        // ToDo: To be implemented
        throw new RuntimeException("Not yet implemented");
    }


    /**
     * TODO: Move to TermHelper
     * @return
     */
    public GenericTerm createGenericTerm() {
        GenericTerm genericTerm = new GenericTerm();
        genericTerm.setZdbID(getZdbID());
        genericTerm.setTermName(getTermName());
        genericTerm.setOboID(getOboID());
        genericTerm.setOntology(getOntology());
        genericTerm.setRoot(isRoot());
        genericTerm.setSecondary(isObsolete());
        genericTerm.setSecondary(isSecondary());
        genericTerm.setComment(getComment());
        genericTerm.setDefinition(getDefinition());

        genericTerm.setAliases(getAliases());
        genericTerm.setImages(getImages());

        return genericTerm;
    }

}
