package org.zfin.gwt.root.ui;

/**
 * If a component can be dirty and can be displayed as such.
 */
public interface IsDirty<T> {

    String NULL_STRING = "null";

    String DIRTY_STYLE = "dirty";
    String CLEAN_STYLE = "table";

    boolean isDirty(T value);
}
