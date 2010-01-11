package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.marker.event.SupplierChangeEvent;

/**
 */
public class SupplierComposite extends Composite {

    // internal data
    private String name ;
    private CanRemoveSupplier parentTable;


    private HorizontalPanel panel = new HorizontalPanel() ;
    private Widget nameLabel = null ;
    private String imageURL = "/images/";
    private Image removeAttributionButton = new Image(imageURL+"delete-button.png") ;

    private boolean visibleName ;

    public SupplierComposite(CanRemoveSupplier parentTable,String attributeName){
        this(parentTable,attributeName,true) ;
    }

    public SupplierComposite(CanRemoveSupplier parentTable,String attributeName,boolean visibleName){
        this.name = attributeName ;
        this.parentTable = parentTable;
        this.visibleName = visibleName ;
        initGui() ;
        initWidget(panel);
    }

    public void setVisibleName(boolean isVisible){
        this.visibleName = isVisible ;
        if(nameLabel!=null){
            nameLabel.setVisible(visibleName);
        }
        removeAttributionButton.setVisible(visibleName);
    }

    private void initGui(){
        nameLabel = new Label(name) ;
        nameLabel.setVisible(visibleName);
        removeAttributionButton.setVisible(visibleName);
        removeAttributionButton.setStyleName("relatedEntityPubLink");

        panel.add(removeAttributionButton);
        panel.add(nameLabel);


        removeAttributionButton.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                parentTable.fireSupplierRemoved(new SupplierChangeEvent(name)) ;
            }
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisibleName() {
        return visibleName;
    }
}