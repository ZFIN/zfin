package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;

import java.util.ArrayList;
import java.util.List;

/**
 * For use with the InferenceListBox.
 */
public class StackComposite<T extends RelatedEntityDTO> extends AbstractComposite<T> implements Revertible {

    protected final HorizontalPanel panel = new HorizontalPanel();
    protected final HTML nameLabel = new HTML();
    protected final String imageURL = "/images/";
    protected final Image removeAttributionButton = new Image(imageURL + "delete-button.png");
    protected List<RelatedEntityListener<T>> relatedEntityEventListeners = new ArrayList<RelatedEntityListener<T>>();

    // internal dirty state
    private boolean dirty = false;

    public StackComposite() {
    }

    public StackComposite(T dto) {
        initGUI();
        setDTO(dto);
        addInternalListeners(this);
        initWidget(panel);
    }

    @Override
    protected void revertGUI() {
        nameLabel.setText(dto.getName());
    }

    @Override
    protected void setValues() {
    }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {

        removeAttributionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                working();
                fireRelatedEntityRemoved(new RelatedEntityEvent<T>(dto));
            }
        });
    }

    public void working() {
        removeAttributionButton.setVisible(false);
    }

    public void notWorking() {
        removeAttributionButton.setVisible(true);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        handleDirty();
    }

    @Override
    public boolean isDirty() {
        return dirty;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean handleDirty() {
        boolean dirty = isDirty();
        if (dirty) {
            nameLabel.setStyleName(IsDirty.DIRTY_STYLE);
        } else {
            nameLabel.setStyleName(IsDirty.CLEAN_STYLE);
        }
        return dirty;
    }

    public void fireRelatedEntityRemoved(RelatedEntityEvent<T> event) {
        for (RelatedEntityListener<T> relatedEntityListener : relatedEntityEventListeners) {
            relatedEntityListener.removeRelatedEntity(event);
        }
    }

    public void addRelatedEntityListener(RelatedEntityListener<T> listener) {
        relatedEntityEventListeners.add(listener);
    }


    public void initGUI() {
        removeAttributionButton.setStyleName("clickable");
        panel.add(removeAttributionButton);
        panel.add(nameLabel);
    }


}