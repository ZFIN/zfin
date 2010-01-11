package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.PublicationDTO;

/**
 */
public class PublicationDisplayPanel extends VerticalPanel {

    private HTML titleLabel = new HTML();
    private HTML authorsLabel = new HTML();
    private Hyperlink zdbIDLink = new Hyperlink();

    // internal data
    private PublicationDTO publicationAbstractDTO;
    private boolean validPub = false;

    public PublicationDisplayPanel() {
        setStyleName("publicationAbstract");

        zdbIDLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=" + publicationAbstractDTO.getZdbID(), "", "");
            }
        });
        add(new HTML("<br>"));
        add(titleLabel);
        HorizontalPanel linkPanel = new HorizontalPanel();
        linkPanel.setSpacing(10);
        linkPanel.add(authorsLabel);
        linkPanel.add(zdbIDLink);
        add(linkPanel);
        setVisible(false);
    }

    public PublicationDTO getPublicationAbstractDomain() {
        return publicationAbstractDTO;
    }

    public void setPublicationAbstractDomain(PublicationDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
        refreshGUI();
    }

    protected void refreshGUI() {
        if (publicationAbstractDTO != null) {
            titleLabel.setVisible(true);
            authorsLabel.setVisible(true);
            zdbIDLink.setVisible(true);
            titleLabel.setHTML("<strong>" + publicationAbstractDTO.getTitle() + "</strong>");
            authorsLabel.setHTML(publicationAbstractDTO.getAuthors());
            zdbIDLink.setText(publicationAbstractDTO.getZdbID());
            validPub = true;
        } else {
            setError();
        }
    }

    public void setError() {
        authorsLabel.setVisible(false);
        zdbIDLink.setVisible(false);
        titleLabel.setHTML("<strong><font color=red>NO PUB SELECTED</font></strong>");
        validPub = false;
    }

    public boolean isValidPub() {
        return validPub;
    }
}