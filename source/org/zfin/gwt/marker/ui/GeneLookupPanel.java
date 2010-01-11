package org.zfin.gwt.marker.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 */
public class GeneLookupPanel extends Composite {

    // gui elements
    private Panel statusPanel = new HorizontalPanel() ;
    private Frame miniGeneFrame = new Frame() ;
    private Hyperlink hideHTML = new Hyperlink() ;
    private Hyperlink showHTML = new Hyperlink() ;
    private Label statusLabel = new Label() ;
    private Panel panel = new VerticalPanel() ;

    // strings
    private String BASE_URL= "/action/marker/mini-gene?zdbID=" ;
    private String EXTERNAL_URL= "&external=true" ;

    public GeneLookupPanel(){

        initGUI() ;

        initWidget(panel);
    }


    public void initGUI(){
        hide() ;
//        hideHTML.setStyleName("attributionPubLink");
//        showHTML.setStyleName("attributionPubLink");
        statusLabel.setStyleName("gwt-lookup-error");
        hideHTML.setTargetHistoryToken("geneLookup");
        showHTML.setTargetHistoryToken("geneLookup");
        hideHTML.setHTML("<a href=#geneLookup><img align=\"top\" src=\"/images/down.gif\" >Hide Lookup</a>");
        showHTML.setHTML("<a href=#geneLookup><img align=\"top\" src=\"/images/right.gif\" >Show Lookup</a>");


        statusPanel.add(hideHTML);
        statusPanel.add(showHTML);
        statusPanel.add(statusLabel);
        panel.add(statusPanel);
        panel.add(miniGeneFrame);

        hideHTML.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                hide() ;
            }
        });


        showHTML.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                show() ;
            }
        });
    }

    public void hide(){
        hideHTML.setVisible(false);
        showHTML.setVisible(true);
//        statusLabel.setVisible(false);
        miniGeneFrame.setVisible(false);
    }

    public void show(){
        hideHTML.setVisible(true);
        showHTML.setVisible(false);
//        statusLabel.setVisible(false);
        miniGeneFrame.setVisible(true);
    }

    public void showZdbID(String zdbID){
        miniGeneFrame.setUrl(BASE_URL+zdbID+EXTERNAL_URL);
        statusLabel.setVisible(false);
        show() ;
    }

    public void setNotFound(String attribution){
        statusLabel.setVisible(true);
        miniGeneFrame.setVisible(false);
        statusLabel.setText("Not found '"+attribution+"'");
    }


    public void setManyFound(String attribution){
        statusLabel.setVisible(true);
        statusLabel.setText("Several found matching '"+attribution+"*'");
        miniGeneFrame.setVisible(false);
    }

}
