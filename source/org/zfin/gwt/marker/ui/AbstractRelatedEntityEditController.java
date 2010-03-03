package org.zfin.gwt.marker.ui;

import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for editing markers.
 */
public abstract class AbstractRelatedEntityEditController<T extends RelatedEntityDTO>  implements PublicationChangeListener, HandlesError, StandardDivNames {


    // internal data
    String publicationZdbID;
    T dto ;

    // listeners
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();
    public static final String LOOKUP_ZDBID = "zdbID";
    public static final String PUB_ZDBID = "pubID";

    /**
     * Creates the user-interface
     */
    public abstract void initGUI();

    /**
     * Load the DTO from accompanying javascript.
     */
    protected abstract void loadDTO();

    /**
     * Sets accompanying interface values either directly or by making RPC calls.
     */
    protected abstract void setValues();

    /**
     * Wire handlers between different GUI components.
     */
    protected abstract void addListeners();


    /**
     * Set DTO in the interface.
     * @param dto DTO to set.
     */
    void setDTO(T dto) {
        this.dto = dto ;
    }

    @Override
    public void onPublicationChanged(PublicationChangeEvent event) {
        publicationZdbID = event.getPublication();
    }

    @Override
    public void setError(String message) {
        // not doing anything with this
    }

    @Override
    public void clearError() {
        fireEventSuccess();
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    void synchronizeHandlesErrorListener(HandlesError handlesError) {
        addHandlesErrorListener(handlesError);
        handlesError.addHandlesErrorListener(this);
    }

}