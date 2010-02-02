package org.zfin.gwt.root.ui;

/**
 * Component can be reverted and set to "working".
 */
public interface Revertible {

    // GUI
    final String TEXT_WORKING = "working...";
    final String TEXT_SAVE = "Save";
    final String TEXT_REVERT = "Revert";

    boolean isDirty();

    void working() ;

    void notWorking() ;

    boolean checkDirty() ;
}
