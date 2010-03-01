package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.marker.event.RelatedEntityListener;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.IsDirty;
import org.zfin.gwt.root.ui.Revertible;

import java.util.ArrayList;
import java.util.List;

/**
 * For use with the InferenceListBox.
 */
public class StackComposite<T extends RelatedEntityDTO> extends AbstractComposite<T> implements Revertible{

    private final HorizontalPanel panel = new HorizontalPanel() ;
    private final Label nameLabel = new Label();
    private final String imageURL = "/images/";
    private final Image removeAttributionButton = new Image(imageURL+"delete-button.png") ;
    private List<RelatedEntityListener<T>> relatedEntityEventListeners = new ArrayList<RelatedEntityListener<T>>();

    // internal dirty state
    private boolean dirty = false ;

    public StackComposite(T dto){
        initGUI() ;
        setDTO(dto);
        addInternalListeners(this);
        initWidget(panel);
    }

    @Override
    protected void revertGUI() {
        nameLabel.setText(dto.getName());
    }

    @Override
    protected void setValues() { }

    @Override
    protected void addInternalListeners(HandlesError handlesError) {

        removeAttributionButton.addClickHandler(new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                working() ;
                fireRelatedEntityRemoved(new RelatedEntityEvent<T>(dto)) ;
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
        return dirty ;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean handleDirty() {
        boolean dirty = isDirty() ;
        if(dirty){
            nameLabel.setStyleName(IsDirty.DIRTY_STYLE);
        }
        else{
            nameLabel.setStyleName(IsDirty.CLEAN_STYLE);
        }
        return dirty ;
    }

    public void fireRelatedEntityRemoved(RelatedEntityEvent<T> event){
        for(RelatedEntityListener<T> relatedEntityListener: relatedEntityEventListeners){
            relatedEntityListener.removeRelatedEntity(event);
        }
    }

    public void addRelatedEntityListener(RelatedEntityListener<T> listener){
        relatedEntityEventListeners.add(listener) ;
    }


    public void initGUI(){
        removeAttributionButton.setStyleName("clickable");
        panel.add(removeAttributionButton);
        panel.add(nameLabel);
    }


}