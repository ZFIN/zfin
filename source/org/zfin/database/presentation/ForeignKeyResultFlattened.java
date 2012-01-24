package org.zfin.database.presentation;

public class ForeignKeyResultFlattened {

    private ForeignKey rootForeignKey;
    private ForeignKey foreignKey;
    private int numberOfResults;
    private int level;
    private String fullNodeName;

    public ForeignKeyResultFlattened(ForeignKey foreignKeyRelation, int numberOfResults) {
        this.foreignKey = foreignKeyRelation;
        this.numberOfResults = numberOfResults;
    }

    public ForeignKey getRootForeignKey() {
        return rootForeignKey;
    }

    public void setRootForeignKey(ForeignKey rootForeignKey) {
        this.rootForeignKey = rootForeignKey;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public String getFullNodeName() {
        return fullNodeName;
    }

    public void setFullNodeName(String fullNodeName) {
        this.fullNodeName = fullNodeName;
    }
}
