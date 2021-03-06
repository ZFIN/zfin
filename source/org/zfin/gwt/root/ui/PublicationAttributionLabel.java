package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 */
public class PublicationAttributionLabel<U extends RelatedEntityDTO> extends Composite {

    // internal data
    private final CanRemoveReference parent;
    private String publication;
    private final U linkableData;
    private String attributionType;

    // gui components
    private final String imageURL = "/images/";
    private final HorizontalPanel panel = new HorizontalPanel();
    private final Hyperlink pubLink = new Hyperlink();
    private final String associatedName;
    private final Image removeReferenceButton = new Image(imageURL + "delete-attribution.png");

    public PublicationAttributionLabel(CanRemoveReference parent, String publication, String associatedName, U linkableData, String attributionType) {
        this.parent = parent;
        this.publication = publication;
        this.associatedName = associatedName;
        this.linkableData = linkableData;
        this.attributionType = attributionType;

        initGui();
        initWidget(panel);
        if (publication == null || publication.length() == 0) {
            setVisible(false);
        }
    }

    public PublicationAttributionLabel(CanRemoveReference parent, String publication, String associatedName, U linkableData) {
        this(parent, publication, associatedName, linkableData, null);
    }


    private void initGui() {
        pubLink.setStyleName("externalLink");
        removeReferenceButton.setStyleName("relatedEntityPubLink");
        removeReferenceButton.setTitle("Remove reference.");

        setEditable((linkableData != null && linkableData.isEditable()));
        pubLink.setText(publication);

        panel.add(removeReferenceButton);
        if (attributionType != null
                &&
                false == attributionType.equals("standard")
                ) {
            panel.add(new Label("(" + attributionType + ")"));
        }
        panel.add(pubLink);

        pubLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.open("http://zfin.org/" + pubLink.getText(), "", "");
            }
        });

        removeReferenceButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                linkableData.setPublicationZdbID(publication);
                parent.removeAttribution(linkableData);
            }
        });
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
        pubLink.setText(this.publication);
    }

    public void clearPublication() {
        setPublication("");
        removeReferenceButton.setVisible(false);
    }

    public String getAssociatedName() {
        return associatedName;
    }

    public boolean isClear() {
        return (publication.length() == 0 || false == removeReferenceButton.isVisible());
    }

    public void setEditable(boolean editable) {
        removeReferenceButton.setVisible(editable);
    }

    public String getAttributionType() {
        return attributionType;
    }

    public void setAttributionType(String type) {
        this.attributionType = type;
    }
}
