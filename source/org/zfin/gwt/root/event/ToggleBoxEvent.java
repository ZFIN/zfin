package org.zfin.gwt.root.event;

/**
 */
public class ToggleBoxEvent {

    private boolean open ;

    public ToggleBoxEvent(boolean open ){
        this.open = open ;
    }

    public boolean isOpen() {
        return open;
    }
}
