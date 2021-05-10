package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A base header class for MarkerDTO's.
 */
public abstract class AbstractHeaderEdit<T extends RelatedEntityDTO> extends AbstractRevertibleComposite<T> {


    // GUI name/type elements
    protected final Label zdbIDLabel = new Label("ZdbID: ");
    protected final HTML zdbIDHTML = new HTML();
    protected final StringTextBox nameBox = new StringTextBox();

    // validator
    protected final NameValidator nameValidator = new NameValidator();

    protected abstract void sendUpdates();

    @Override
    protected void setValues() {
    }

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

    public void addChangeListener(RelatedEntityChangeListener<T> relatedEntityChangeListener) {
        changeListeners.add(relatedEntityChangeListener);
    }

}
