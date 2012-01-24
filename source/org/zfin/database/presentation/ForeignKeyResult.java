package org.zfin.database.presentation;

import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public class ForeignKeyResult {

    private ForeignKey foreignKey;
    private int numberOfResults;
    private List<ForeignKeyResult> children;
    private Map<String, ForeignKeyResult> nodeMap;
    private String fullNodeName;

    public ForeignKeyResult(ForeignKey foreignKeyRelation, int numberOfResults) {
        this.foreignKey = foreignKeyRelation;
        this.numberOfResults = numberOfResults;
    }

    /**
     * Store all pathways in a map for easy access of each node
     */
    private void buildMap() {
        nodeMap = new HashMap<String, ForeignKeyResult>();
        String baseNodeName = foreignKey.getForeignKey();
        nodeMap.put(baseNodeName, this);
        if (!hasChildren()) {
            return;
        }
        for (ForeignKeyResult result : getChildren()) {
            String nodeName = baseNodeName + "," + result.getForeignKey().getForeignKey();
            nodeMap.put(nodeName, result);
            if (result.hasChildren()) {
                Map<String, ForeignKeyResult> childMap = result.getNodeMap();
                adjustLocalMap(childMap);
            }
        }
    }

    private void adjustLocalMap(Map<String, ForeignKeyResult> childMap) {
        for (String key : childMap.keySet()) {
            String nodeName = foreignKey.getForeignKey() + "," + key;
            nodeMap.put(nodeName, childMap.get(key));
        }
    }

    public String getNodeName() {
        if (foreignKey.isManyToManyRelationship())
            return foreignKey.getManyToManyTable().getPkName();
        else
            return foreignKey.getForeignKey();
    }

    public ForeignKey getForeignKey() {
        return foreignKey;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public List<ForeignKeyResult> getChildren() {
        return children;
    }

    public boolean hasChildren() {
        return CollectionUtils.isNotEmpty(children);
    }

    public void setChildren(List<ForeignKeyResult> children) {
        this.children = children;
    }

    public void add(ForeignKeyResult foreignKeyResult) {
        if (children == null)
            children = new ArrayList<ForeignKeyResult>(2);
        children.add(foreignKeyResult);
    }

    public Map<String, ForeignKeyResult> getNodeMap() {
        if (nodeMap == null)
            buildMap();
        return nodeMap;
    }

    public boolean hasChildNode(String nodeName) {
        if (nodeMap == null)
            buildMap();
        return nodeMap.containsKey(nodeName);
    }

    public ForeignKeyResult getChildKeyResult(String nodeName) {
        if (nodeMap == null)
            buildMap();
        return nodeMap.get(nodeName);
    }

    public String getFullNodeName() {
        return fullNodeName;
    }

    public void setFullNodeName(String fullNodeName) {
        this.fullNodeName = fullNodeName;
    }
}
