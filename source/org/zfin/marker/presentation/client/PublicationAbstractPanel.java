package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.marker.presentation.dto.PublicationAbstractDTO;

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
    private PublicationAbstractDTO publicationAbstractDTO;

    public PublicationAbstractPanel() {
        setStyleName("publicationAbstract");
        abstractLabel.setWordWrap(true);
        doiLink.setStyleName("attributionPubLink");
        accessionLink.setStyleName("attributionPubLink");
//        zdbIDLink.setStyleName("attributionPubLink");

        doiLink.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                if (publicationAbstractDTO != null && publicationAbstractDTO.getDoi() != null && publicationAbstractDTO.getDoi().length() > 0) {
                    Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubmedredirect.apg&pubID=ZDB-PUB-070210-20&accession_no=" +
                            publicationAbstractDTO.getAccession() + "&referenceurl=MIval=aa-pubview2.apg", "", "");
                }
            }
        });

        accessionLink.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
                if (publicationAbstractDTO != null && publicationAbstractDTO.getAccession() != null && publicationAbstractDTO
                        .getAccession().length() > 0) {
                    Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-externalredirect.apg&pubID=ZDB-PUB-070210-20&newurl=" +
                            publicationAbstractDTO.getDoi() + "&referenceurl=MIval=aa-pubview2.apg", "", "");
                }
            }
        });

        zdbIDLink.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
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

    public PublicationAbstractDTO getPublicationAbstractDomain() {
        return publicationAbstractDTO;
    }

    public void setPublicationAbstractDomain(PublicationAbstractDTO publicationAbstractDTO) {
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
