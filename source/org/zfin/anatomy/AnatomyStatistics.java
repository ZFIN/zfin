package org.zfin.anatomy;

import org.zfin.anatomy.presentation.AnatomyPresentation;
import org.zfin.ontology.GenericTerm;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.text.ChoiceFormat;
import java.util.Set;

/**
 * Please provide JavaDoc info!!!
 */
public class AnatomyStatistics implements Comparable<AnatomyStatistics>, Serializable {

    private String zdbID;
    private AnatomyItem anatomyItem;
    private Type type;
    private int numberOfObjects;
    private int numberOfTotalDistinctObjects;
    private int numberOfSynonyms;
    private AnatomyTreeInfo treeInfo;
    private Set<AnatomyTreeInfo> treeInfos;
    private GenericTerm term;

    // ToDo: move into a formatting class for presentation layer
    private static final ChoiceFormat geneChoice = new ChoiceFormat("0#genes| 1#gene| 2#genes");
    private static final ChoiceFormat synonymChoice = new ChoiceFormat("0#synonyms| 1#synonym| 2#synonyms");
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String TAB = "\t";

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public AnatomyItem getAnatomyItem() {
        if (anatomyItem == null) {
            anatomyItem = RepositoryFactory.getAnatomyRepository().getAnatomyTermByOboID(term.getOboID());
        }
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
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
        return AnatomyPresentation.createFormattedSynonymList(getAnatomyItem());
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
        String compName = anatCompare.getAnatomyItem().getTermName();
        String name = getAnatomyItem().getTermName();
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
        sb.append(getAnatomyItem());
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
        return getAnatomyItem().equals(o);
    }

    public int hashCode() {
        return getAnatomyItem().hashCode();
    }

}
