package org.zfin.indexer;

public enum IndexerTaskType {
    INPUT_OUTPUT("Input/Output"),
    DELETE("Delete"),
    SAVE("Save");

    private String displayName;

    IndexerTaskType(String displayName) {
        this.displayName = displayName;
    }
}
