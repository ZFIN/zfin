package org.zfin.anatomy;

import org.zfin.anatomy.presentation.AnatomyPresentation;

import java.text.ChoiceFormat;
import java.util.Set;

/**
 * Please provide JavaDoc info!!!
 */
public class AnatomyStageStatistics{

    private String zdbID;
    private AnatomyItem anatomyItem;
    private String type;
    private int numberOfObjects;
    private int numberOfTotalDistinctObjects;
    private AnatomyTreeInfo treeInfo;
    private Set<AnatomyTreeInfo> treeInfos;

    // ToDo: move into a formatting class for presentation layer
    private static final ChoiceFormat geneChoice = new ChoiceFormat("0#genes| 1#gene| 2#genes");
    private static final String NEWLINE = "\r";
    private static final String TAB = "\t";

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public AnatomyItem getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        return AnatomyStageStatistics.geneChoice.format(numberOfObjects);
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
        return AnatomyPresentation.createFormattedSynonymList(anatomyItem);
    }


    /**
     * Allow sorting by case insensitive anatomy item name.
     *
     * @param o
     * @return integer that indicates comparison.
     */
    public int compareTo(Object o) {
        AnatomyStageStatistics anatCompare = (AnatomyStageStatistics) o;
        String compName = anatCompare.getAnatomyItem().getName();
        String name = anatomyItem.getName();
        return name.toLowerCase().compareTo(compName.toLowerCase());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AnatomyStatistics:");
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append("zdbID");
        sb.append(AnatomyStageStatistics.TAB);
        sb.append(zdbID);
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append(anatomyItem);
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append("type");
        sb.append(AnatomyStageStatistics.TAB);
        sb.append(type);
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append("numberOfObjects");
        sb.append(AnatomyStageStatistics.TAB);
        sb.append(numberOfObjects);
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append("numberOfTotalDistinctObjects");
        sb.append(AnatomyStageStatistics.TAB);
        sb.append(numberOfTotalDistinctObjects);
        sb.append(AnatomyStageStatistics.NEWLINE);
        sb.append("numberOfSynonyms");

        return sb.toString();
    }
}
