package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import org.zfin.marker.presentation.dto.PublicationAbstractDTO;

/**
 */
public class PublicationDisplayPanel extends VerticalPanel {

    private HTML titleLabel = new HTML() ;
    private HTML authorsLabel = new HTML() ;
    private Hyperlink zdbIDLink = new Hyperlink() ;

    // internal data
    private PublicationAbstractDTO publicationAbstractDTO;
    private boolean validPub = false ;

    public PublicationDisplayPanel(){
        setStyleName("publicationAbstract");

        zdbIDLink.addClickListener(new ClickListener(){
            public void onClick(Widget widget) {
                Window.open("http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID="+ publicationAbstractDTO.getZdbID(),"","");
            }
        });
        add(new HTML("<br>")) ;
        add(titleLabel) ;
        HorizontalPanel linkPanel=new HorizontalPanel();
        linkPanel.setSpacing(10);
        linkPanel.add(authorsLabel) ;
        linkPanel.add(zdbIDLink);
        add(linkPanel) ;
        setVisible(false);
    }

    public PublicationAbstractDTO getPublicationAbstractDomain() {
        return publicationAbstractDTO;
    }

    public void setPublicationAbstractDomain(PublicationAbstractDTO publicationAbstractDTO) {
        this.publicationAbstractDTO = publicationAbstractDTO;
        refreshGUI();
    }

    protected void refreshGUI() {
        if(publicationAbstractDTO!=null){
            titleLabel.setVisible(true);
            authorsLabel.setVisible(true);
            zdbIDLink.setVisible(true);
            titleLabel.setHTML("<strong>"+ publicationAbstractDTO.getTitle()+"</strong>");
            authorsLabel.setHTML(publicationAbstractDTO.getAuthors());
            zdbIDLink.setText(publicationAbstractDTO.getZdbID());
            validPub = true ; 
        }
        else{
            setError();
        }
    }

    public void setError(){
        authorsLabel.setVisible(false);
        zdbIDLink.setVisible(false);
        titleLabel.setHTML("<strong><font color=red>NO PUB SELECTED</font></strong>");
        validPub = false ;
    }

    public boolean isValidPub() {
        return validPub;
    }
}