package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.root.dto.PublicationDTO;

/**
 */
public class PublicationDisplayPanel extends VerticalPanel {

    private HTML titleLabel = new HTML();
    private HTML authorsLabel = new HTML();
    private HTML miniRefLabel = new HTML();
    private Hyperlink zdbIDLink = new Hyperlink();
    private Grid  table = new Grid(4,2);

    // internal data
    private PublicationDTO publicationAbstractDTO;
    private boolean validPub = false;
    private HTML noPubLabel = new HTML("<strong><font color=red>NO PUB SELECTED</font></strong>");
    private static final int MAX_LENGTH = 50;

    public PublicationDisplayPanel() {
        setStyleName("publicationAbstract");
        zdbIDLink.setStyleName("externalLink");

        zdbIDLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=" + publicationAbstractDTO.getZdbID(), "", "");
            }
        });

        table.setWidth("400px");

        int rowCount = 0 ;
        table.setWidget(rowCount,1,miniRefLabel);
        ++rowCount;
        table.setWidget(rowCount,0,new HTML("<b>Title:</b>"));
        table.setWidget(rowCount,1,titleLabel);
        ++rowCount;
        table.setWidget(rowCount,0,new HTML("<b>Author(s):</b>"));
        table.setWidget(rowCount,1,authorsLabel);
        ++rowCount;
        table.setWidget(rowCount,0,new HTML("<b>Link:</b>"));
        table.setWidget(rowCount,1,zdbIDLink);

        add(table) ;
        add(noPubLabel);

        setHasNoPub();
    }

    public void setPublicationAbstractDomain(PublicationDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
        refreshGUI();
    }

    protected void refreshGUI() {
        if (publicationAbstractDTO != null) {
            setHasPub();
        } else {
            setHasNoPub();
        }
    }

    public void setHasPub(){
        table.setVisible(true);
        noPubLabel.setVisible(false);
        titleLabel.setHTML("<strong>" + publicationAbstractDTO.getTitle() + "</strong>");
        if(publicationAbstractDTO.getAuthors().length()>MAX_LENGTH){
            authorsLabel.setHTML(publicationAbstractDTO.getAuthors());
        }
        else{
            authorsLabel.setHTML(publicationAbstractDTO.getAuthors());
        }
        zdbIDLink.setText(publicationAbstractDTO.getZdbID());
        miniRefLabel.setHTML(publicationAbstractDTO.getMiniRef());
        validPub = true;
    }

    public void setHasNoPub() {
        table.setVisible(false);
        noPubLabel.setVisible(true);
        validPub = false;
    }

    public boolean isValidPub() {
        return validPub;
    }
}