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
public class PublicationAbstractPanel extends VerticalPanel {

    private HTML titleLabel = new HTML();
    private HTML authorsLabel = new HTML();
    private HTML citationLabel = new HTML();
    private Hyperlink zdbIDLink = new Hyperlink();
    private HTML abstractLabel = new HTML();
    private Hyperlink accessionLink = new Hyperlink();
    private Hyperlink doiLink = new Hyperlink();

    // internal data
    private PublicationDTO publicationAbstractDTO;

    public PublicationAbstractPanel() {
        setStyleName("publicationAbstract");
        abstractLabel.setWordWrap(true);
        doiLink.setStyleName("attributionPubLink");
        accessionLink.setStyleName("attributionPubLink");
//        zdbIDLink.setStyleName("attributionPubLink");

        doiLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (publicationAbstractDTO != null && publicationAbstractDTO.getDoi() != null && publicationAbstractDTO.getDoi().length() > 0) {
                    Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubmedredirect.apg&pubID=ZDB-PUB-070210-20&accession_no=" +
                            publicationAbstractDTO.getAccession() + "&referenceurl=MIval=aa-pubview2.apg", "", "");
                }
            }
        });

        accessionLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (publicationAbstractDTO != null && publicationAbstractDTO.getAccession() != null && publicationAbstractDTO
                        .getAccession().length() > 0) {
                    Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-externalredirect.apg&pubID=ZDB-PUB-070210-20&newurl=" +
                            publicationAbstractDTO.getDoi() + "&referenceurl=MIval=aa-pubview2.apg", "", "");
                }
            }
        });

        zdbIDLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=" + publicationAbstractDTO.getZdbID(), "", "");
            }
        });

        add(new HTML("<br>"));
        add(titleLabel);
        add(authorsLabel);
        add(new HTML("<hr>"));
        add(citationLabel);
        add(new HTML("<hr>"));
        HorizontalPanel linkPanel = new HorizontalPanel();
        linkPanel.setSpacing(10);
        linkPanel.add(zdbIDLink);
        linkPanel.add(accessionLink);
        linkPanel.add(doiLink);
        add(linkPanel);
        add(new HTML("<hr>"));
        add(abstractLabel);
        setVisible(false);
    }

    public PublicationDTO getPublicationAbstractDomain() {
        return publicationAbstractDTO;
    }

    public void setPublicationAbstractDomain(PublicationDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
        initGUI();
    }

    protected void initGUI() {
        titleLabel.setHTML("<strong>" + publicationAbstractDTO.getTitle() + "</strong>");
        authorsLabel.setHTML(publicationAbstractDTO.getAuthors());
        zdbIDLink.setText(publicationAbstractDTO.getZdbID());
        citationLabel.setHTML(publicationAbstractDTO.getCitation());

        // new links
        if (publicationAbstractDTO.getDoi() != null && publicationAbstractDTO.getDoi().length() > 0) {
            doiLink.setHTML(publicationAbstractDTO.getDoi());
            doiLink.setVisible(true);
        } else {
            doiLink.setVisible(false);
        }

        if (publicationAbstractDTO.getAccession() != null && publicationAbstractDTO.getAccession().length() > 0 && false ==
                publicationAbstractDTO.getAccession().equalsIgnoreCase("none")) {
            accessionLink.setHTML(publicationAbstractDTO.getAccession());
            accessionLink.setVisible(true);
        } else {
            accessionLink.setVisible(false);
        }

        abstractLabel.setHTML(publicationAbstractDTO.getAbstractText());
    }
}
