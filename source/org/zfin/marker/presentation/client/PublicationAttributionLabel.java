package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import org.zfin.marker.presentation.dto.RelatedEntityDTO;

/**
 */
public class PublicationAttributionLabel<U extends RelatedEntityDTO> extends Composite  {

    // internal data
    private CanRemoveReference parent;
    private String publication ;
    private U linkableData;
    private String attributionType;

    // gui components
    //    private String imageURL = "/gwt/org.zfin.marker.presentation.Marker/";
    private String imageURL = "/images/";
    private HorizontalPanel panel = new HorizontalPanel();
    private Hyperlink pubLink = new Hyperlink() ;
    private String associatedName ;
    private Image removeReferenceButton = new Image(imageURL+"delete-attribution.png");

    public PublicationAttributionLabel(CanRemoveReference parent,String publication,String associatedName,U linkableData,String attributionType){
        this.parent = parent;
        this.publication = publication;
        this.associatedName = associatedName ;
        this.linkableData = linkableData ;
        this.attributionType = attributionType;

        initGui() ;
        initWidget(panel);
        if(publication==null || publication.length()==0){
            setVisible(false);
        }
    }

    public PublicationAttributionLabel(CanRemoveReference parent,String publication,String associatedName,U linkableData){
        this(parent,publication, associatedName,linkableData,null) ;
    }


    private void initGui(){
        pubLink.setStyleName("externalLink");
        removeReferenceButton.setStyleName("relatedEntityPubLink");
        removeReferenceButton.setTitle("Remove reference.");

        setEditable((linkableData != null && linkableData.isEditable()));
        pubLink.setText(publication);

        panel.add(removeReferenceButton);
        if(attributionType !=null
                &&
                false==attributionType.equals("standard")
                ){
            panel.add(new Label("("+attributionType+")"));
        }
        panel.add(pubLink);

        pubLink.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
//                Window.open("http://zfin.org/cgi-bin/webdriver/?MIval=aa-pubview2.apg&OID=ZDB-PUB-070210-20", "", "");
                Window.open("http://zfin.org/cgi-bin/webdriver/?MIval=aa-pubview2.apg&OID="+pubLink.getText(), "", "");
            }
        });

        removeReferenceButton.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                linkableData.setPublicationZdbID(publication);
                parent.removeAttribution(linkableData) ;
            }
        });
    }

    public String getPublication(){
        return publication ;
    }

    public void setPublication(String publication) {
        this.publication = publication ;
        pubLink.setText(this.publication);
    }

    public void clearPublication() {
        setPublication("");
        removeReferenceButton.setVisible(false);
    }

    public String getAssociatedName() {
        return associatedName;
    }

    public boolean isClear(){
        return (publication.length()==0 || false==removeReferenceButton.isVisible() ) ;
    }

    public void setEditable(boolean editable){
        removeReferenceButton.setVisible(  editable );
    }

    public String getAttributionType() {
        return attributionType;
    }

    public void setAttributionType(String type) {
        this.attributionType = type;
    }
}
