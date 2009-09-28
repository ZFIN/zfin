package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.RelatedEntityDTO;

/**
 */
public class RelatedEntityLabel<U extends RelatedEntityDTO> extends Composite {

    // internal data
    private String name ;
    private HasRelatedEntities parentTable;
    // this is just meta-data
    private U linkableData;


    private HorizontalPanel panel = new HorizontalPanel() ;
    private HTML displayHTML = null ;
    private String imageURL = "/images/";
    private Image addAttributionButton = new Image(imageURL+"new-attribution.png");
    private Image removeRelatedEntityButton = new Image(imageURL+"delete-button.png") ;

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

        addAttributionButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                parentTable.addAttribution(getAttributeNameWidget().getRelatedEntityDTO()) ;
            }
        });

        removeRelatedEntityButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                parentTable.removeRelatedEntity(getAttributeNameWidget().getRelatedEntityDTO()) ;
            }
        });
    }

    public RelatedEntityLabel getAttributeNameWidget(){
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
