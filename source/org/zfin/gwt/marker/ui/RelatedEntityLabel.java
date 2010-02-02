package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 * A label for related entities.
 */
public class RelatedEntityLabel<U extends RelatedEntityDTO> extends Composite {

    // internal data
    private String name ;
    private final HasRelatedEntities parentTable;
    // this is just meta-data
    private U linkableData;


    private final HorizontalPanel panel = new HorizontalPanel() ;
    private HTML displayHTML = null ;
    private final String imageURL = "/images/";
    private final Image addAttributionButton = new Image(imageURL+"new-attribution.png");
    private final Image removeRelatedEntityButton = new Image(imageURL+"delete-button.png") ;

    private boolean visibleName ;

    public RelatedEntityLabel(HasRelatedEntities parentTable,String attributeName,U linkableData){
        this(parentTable,attributeName,true,linkableData) ;
    }

    public RelatedEntityLabel(HasRelatedEntities parentTable,String attributeName,boolean visibleName, U linkableData){
        this.name = attributeName ;
        this.parentTable = parentTable;
        this.visibleName = visibleName ;
        this.linkableData = linkableData;
        initGui() ;
        initWidget(panel);
    }

    public void setVisibleName(boolean isVisible){
        this.visibleName = isVisible ;
        if(displayHTML !=null){
            displayHTML.setVisible(visibleName);
        }
        removeRelatedEntityButton.setVisible(visibleName && linkableData.isEditable());
        addAttributionButton.setVisible(visibleName  && linkableData.isEditable());

    }

    private void initGui(){
        displayHTML = new HTML() ;
        if(linkableData!=null && linkableData.getLink()!=null){
//            displayHTML.setStyleName("externalLink");
            displayHTML.setHTML(linkableData.getLink());
        }
        else{
            displayHTML.setText(name);
        }

        removeRelatedEntityButton.setStyleName("relatedEntityPubLink");
        removeRelatedEntityButton.setTitle("Remove association.");
        addAttributionButton.setStyleName("relatedEntityPubLink");
        addAttributionButton.setTitle("Add attribution.");

        displayHTML.setVisible(visibleName);
        removeRelatedEntityButton.setVisible(visibleName && linkableData.isEditable());
        addAttributionButton.setPixelSize(20,20);
        addAttributionButton.setVisible(visibleName && linkableData.isEditable());

        panel.add(removeRelatedEntityButton);
        panel.add(displayHTML);
        panel.add(addAttributionButton);

        addAttributionButton.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                parentTable.addAttribution(getAttributeNameWidget().getRelatedEntityDTO()) ;
            }
        });

        removeRelatedEntityButton.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                parentTable.removeRelatedEntity(getAttributeNameWidget().getRelatedEntityDTO()) ;
            }
        });
    }

    RelatedEntityLabel getAttributeNameWidget(){
        return this ;
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

    //todo: I think I need to refactor linableData to just be relatedEntityDTO
    public U getRelatedEntityDTO() {
        return linkableData;
    }

    public  U getLinkableData() {
        return linkableData;
    }

    public  void setLinkableData(U linkableData) {
        this.linkableData = linkableData;
    }

}
