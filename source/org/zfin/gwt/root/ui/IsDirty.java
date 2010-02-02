package org.zfin.gwt.root.ui;

/**
 * If a component can be dirty and can be displayed as such.
 */
public interface IsDirty<T> {

    static final String DIRTY_STYLE = "dirty";
    static final String CLEAN_STYLE = "table";

    boolean isDirty(T value) ;
}
