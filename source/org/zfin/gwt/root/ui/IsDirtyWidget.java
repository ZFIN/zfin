package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * If a component can be dirty and can be displayed as such.
 */
public interface IsDirtyWidget<T> extends IsWidget {

    String NULL_STRING = "null";

    String DIRTY_STYLE = "dirty";
    String CLEAN_STYLE = "table";

    boolean isDirty(T value);
}
