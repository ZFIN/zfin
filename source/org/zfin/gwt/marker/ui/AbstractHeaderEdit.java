package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.marker.event.RelatedEntityChangeListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.StringTextBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A base header class for MarkerDTO's.
 */
public abstract class AbstractHeaderEdit<T extends MarkerDTO>  extends AbstractRevertibleComposite<T> {



    // GUI name/type elements
    final Label zdbIDLabel = new Label("ZdbID: ");
    final HTML zdbIDHTML = new HTML();
    final StringTextBox nameBox = new StringTextBox();

    // validator
    protected final NameValidator nameValidator = new NameValidator();

    protected abstract void sendUpdates() ;

    // listeners
    private final List<RelatedEntityChangeListener<T>> changeListeners = new ArrayList<RelatedEntityChangeListener<T>>();


    @Override
    public void working() {
        super.working();
        nameBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();   
        nameBox.setEnabled(true);
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {
        nameBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                DeferredCommand.addCommand(new CompareCommand());
            }
        });


        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                sendUpdates();
            }
        });

        revertButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                revertGUI();
            }
        });
    }

    protected void fireChangeEvent(RelatedEntityEvent<T> relatedEntityEvent) {
        for (RelatedEntityChangeListener<T> changeListener : changeListeners) {
            changeListener.dataChanged(relatedEntityEvent);
        }
    }

    public void addChangeListener(RelatedEntityChangeListener<T> relatedEntityChangeListener){
        changeListeners.add(relatedEntityChangeListener);
    }

}
