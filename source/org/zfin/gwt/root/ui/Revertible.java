package org.zfin.gwt.root.ui;

/**
 * Component can be reverted and set to "working".
 */
public interface Revertible {

    // GUI
    final String TEXT_WORKING = "working...";
    final String TEXT_SAVE = "Save";
    final String TEXT_REVERT = "Revert";

    /**
     * Checks if the composite has been changed from its underlying DTO.
     * @return If the composites has unsaved changes that don't match the internal DTO.
     */
    boolean isDirty();

    /**
     * Handles what happens if the composite is dirty.  Will typically call isDirty to check.
     * @return If the composite is dirty.
     */
    boolean handleDirty() ;

    /**
     * Sets status of composites to working and NOT available.
     */
    void working() ;

    /**
     * Sets status of composites to not working and AVAILABLE.
     */
    void notWorking() ;

}
