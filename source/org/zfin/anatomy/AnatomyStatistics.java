package org.zfin.anatomy;

import org.zfin.anatomy.presentation.AnatomyPresentation;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.io.Serializable;
import java.text.ChoiceFormat;
import java.util.Set;

/**
 * Please provide JavaDoc info!!!
 */
@Entity
@Table(name = "ANATOMY_STATS")
public class AnatomyStatistics implements Comparable<AnatomyStatistics>, Serializable {

    @Id
    @Column(name = "anatstat_term_zdb_id")
    private String zdbID;
    @Id
    @Column(name = "anatstat_object_type")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.anatomy.AnatomyStatistics$Type")})
    private Type type;
    @Column(name = "anatstat_object_count")
    private int numberOfObjects;
    @Column(name = "anatstat_total_distinct_count")
    private int numberOfTotalDistinctObjects;
    @Column(name = "anatstat_synonym_count")
    private int numberOfSynonyms;
    @Transient
    private AnatomyTreeInfo treeInfo;
    @Transient
    private Set<AnatomyTreeInfo> treeInfos;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anatstat_term_zdb_id")
    private GenericTerm term;

    // ToDo: move into a formatting class for presentation layer
    private static final ChoiceFormat geneChoice = new ChoiceFormat("0#genes| 1#gene| 2#genes");
    private static final ChoiceFormat synonymChoice = new ChoiceFormat("0#synonyms| 1#synonym| 2#synonyms");
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String TAB = "\t";

    public AnatomyStatistics() {
    }

    public AnatomyStatistics(GenericTerm term) {
        this.term = term;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public Type getType() {
        return type;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public int getNumberOfTotalDistinctObjects() {
        return numberOfTotalDistinctObjects;
    }

    public void setNumberOfTotalDistinctObjects(int numberOfTotalDistinctObjects) {
        this.numberOfTotalDistinctObjects = numberOfTotalDistinctObjects;
    }

    public String getFormattedGeneNumber() {
        return geneChoice.format(numberOfObjects);
    }

    public String getFormattedSynonymNumber() {
        return synonymChoice.format(numberOfSynonyms);
    }

    public int getNumberOfSynonyms() {
        return numberOfSynonyms;
    }

    public void setNumberOfSynonyms(int numberOfSynonyms) {
        this.numberOfSynonyms = numberOfSynonyms;
    }

    public AnatomyTreeInfo getTreeInfo() {
        return treeInfo;
    }

    public void setTreeInfo(AnatomyTreeInfo treeInfo) {
        this.treeInfo = treeInfo;
    }

    public Set<AnatomyTreeInfo> getTreeInfos() {
        return treeInfos;
    }

    public void setTreeInfos(Set<AnatomyTreeInfo> treeInfos) {
        this.treeInfos = treeInfos;
    }

    //ToDo: Move comma delimited list into utility class.

    public String getFormattedSynonymList() {
        return AnatomyPresentation.createFormattedSynonymList(getTerm());
    }

    public String getIndentationLevel() {
        if (treeInfo == null)
            return "0";
        else
            return "" + (treeInfo.getIndent() - 1) * 10;
    }


    /**
     * Allow sorting by case insensitive anatomy item name.
     *
     * @param anatCompare
     * @return integer that indicates comparison.
     */
    public int compareTo(AnatomyStatistics anatCompare) {
        String compName = anatCompare.getTerm().getTermName();
        String name = getTerm().getTermName();
        return name.compareToIgnoreCase(compName);
    }

    public enum Type {
        GENE, GENO;

        public String toString() {
            return name();
        }


    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AnatomyStatistics:");
        sb.append(NEWLINE);
        sb.append("zdbID");
        sb.append(TAB);
        sb.append(zdbID);
        sb.append(NEWLINE);
        sb.append(getTerm());
        sb.append(NEWLINE);
        sb.append("type");
        sb.append(TAB);
        sb.append(type);
        sb.append(NEWLINE);
        sb.append("numberOfObjects");
        sb.append(TAB);
        sb.append(numberOfObjects);
        sb.append(NEWLINE);
        sb.append("numberOfTotalDistinctObjects");
        sb.append(TAB);
        sb.append(numberOfTotalDistinctObjects);
        sb.append(NEWLINE);
        sb.append("numberOfSynonyms");
        sb.append(TAB);
        sb.append(numberOfSynonyms);

        return sb.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof AnatomyStatistics))
            return false;
        return getTerm().equals(o);
    }

    public int hashCode() {
        return getTerm().hashCode();
    }

}
