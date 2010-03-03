package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.marker.event.PublicationChangeEvent;
import org.zfin.gwt.marker.event.PublicationChangeListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.marker.event.RelatedEntityListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;


/**
 * Base class for related entity box.  Allows for building sequences, as well.
 */
public abstract class AbstractRelatedEntityContainer<U extends RelatedEntityDTO>
        extends Composite
        implements HandlesError, HasRelatedEntities, PublicationChangeListener, RequiresAttribution {

    // internal data
    private String zdbID = null;
    private boolean attributionRequired = true;

    // listeners
    private final List<RelatedEntityListener<U>> relatedEntityListeners = new ArrayList<RelatedEntityListener<U>>();
    private final List<HandlesError> handlesErrorListeners = new ArrayList<HandlesError>();

    // common GUI elements
    final Label publicationLabel = new Label();

    // error label
    final Label errorLabel = new Label();

    protected abstract List<String> getRelatedEntityAttributionsForName(String relatedEntityName);

    protected abstract List<String> getRelatedEntityNames();

    protected abstract List<U> getRelatedEntityDTOs();

    public abstract void addRelatedEntityToGUI(U relatedEntityDTO);

    public abstract void removeRelatedEntityFromGUI(U relatedEntityDTO);

    public abstract boolean isDirty() ;

    AbstractRelatedEntityContainer() {
    }

    public AbstractRelatedEntityContainer(boolean attributionRequired) {
        setAttributionRequired(attributionRequired);
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    String getPublication() {
        return publicationLabel.getText().trim();
    }

    void setPublication(String publicationZdbID) {
        publicationLabel.setText(publicationZdbID);
    }


    public void setError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText("");
    }

    public boolean isAttributionRequired() {
        return attributionRequired;
    }

    public void setAttributionRequired(boolean attributionRequired) {
        this.attributionRequired = attributionRequired;
    }

    /**
     * GUI functions
     */
    /**
     * Clears the from
     */
    void reset() {
        for (U dto : getRelatedEntityDTOs()) {
            removeRelatedEntity(dto);
        }
    }

    public void setRelatedEntities(String zdbID, List<U> relatedEntityList) {
        if(relatedEntityList!=null){
            this.zdbID = zdbID;
            reset();
            for (U relatedEntityDTO : relatedEntityList) {
                addRelatedEntityToGUI(relatedEntityDTO);
            }
        }
    }


    String validateNewAttribution(RelatedEntityDTO dto) {
        String name = dto.getName();
        String pub = dto.getPublicationZdbID();

        if (pub == null || pub.length() == 0) {
            return "Publication must be selected to add new reference.";
        }
        List<String> relatedEntityAttributions = getRelatedEntityAttributionsForName(name);
        if (getRelatedEntityIndex(name) < 0) {
            return "Entity name does not exist [" + name + "]";
        }
        if (relatedEntityAttributions.contains(pub)) {
            return "Publication [" + pub + "] exists for [" + name + "]";
        }

        return null;
    }

    int getRelatedEntityIndex(String name) {
        List relatedEntityNames = getRelatedEntityNames();
        return (relatedEntityNames.indexOf(name));
    }


    public void removeRelatedEntity(final RelatedEntityDTO relatedEntityDTO) {
        fireRelatedEntityRemoved(new RelatedEntityEvent(relatedEntityDTO));
    }

    /**
     * This method returns an error string to be displayed.
     *
     * @param name Name of the related entity to add.
     * @return Error string to be displayed.
     */
    String validateNewRelatedEntity(String name) {
        if (getRelatedEntityIndex(name) >= 0) {
            return "Related entity [" + name + "] exists.";
        }
        return null;
    }

    public void addAttribution(RelatedEntityDTO relatedEntityDTO) {
        RelatedEntityDTO newDTO = relatedEntityDTO.deepCopy();
        newDTO.setPublicationZdbID(getPublication());

        String errorString = validateNewAttribution(newDTO);
        if (errorString != null) {
            setError(errorString);
            return;
        }
        fireAttributionAdded(new RelatedEntityEvent(newDTO));
    }

    //// Handle listeners

    public void addRelatedEntityCompositeListener(RelatedEntityListener<U> relatedEntityListener) {
        relatedEntityListeners.add(relatedEntityListener);
    }

    void fireRelatedEntityAdded(RelatedEntityEvent<U> relatedEntityEvent) {
        fireEventSuccess();
        for (RelatedEntityListener<U> relatedEntityListener : relatedEntityListeners) {
            relatedEntityListener.addRelatedEntity(relatedEntityEvent);
        }
    }

    void fireAttributionAdded(RelatedEntityEvent<U> relatedEntityEvent) {
        fireEventSuccess();
        for (RelatedEntityListener<U> relatedEntityListener : relatedEntityListeners) {
            relatedEntityListener.addAttribution(relatedEntityEvent);
        }
    }


    void fireRelatedEntityRemoved(RelatedEntityEvent<U> relatedEntityEvent) {
        fireEventSuccess();
        for (RelatedEntityListener<U> relatedEntityListener : relatedEntityListeners) {
            relatedEntityListener.removeRelatedEntity(relatedEntityEvent);
        }
    }

    void fireAttributionRemoved(RelatedEntityEvent<U> relatedEntityEvent) {
        fireEventSuccess();
        for (RelatedEntityListener<U> relatedEntityListener : relatedEntityListeners) {
            relatedEntityListener.removeAttribution(relatedEntityEvent);
        }
    }

    public void onPublicationChanged(PublicationChangeEvent event) {
        fireEventSuccess();
        setPublication(event.getPublication());
    }

    boolean attributionIsValid() {
        if (isAttributionRequired()
                && (publicationLabel.getText() == null
                || publicationLabel.getText().equals(""))) {
            setError("Attribution Required.");
            return false;
        }
        return true;
    }

    public void fireEventSuccess() {
        clearError();
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }


    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

}
