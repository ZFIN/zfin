package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.*;

/**
 */
public class PhenotePopup extends PopupPanel {

    private final String BASE_URL =  "/action/anatomy/term-info?anatomyItem.zdbID=" ;
    private Frame frame ;

    public PhenotePopup(){
        super(true,true) ;
        initGUI();
    }

    public PhenotePopup(String zdbID){
        this() ;
        showPopup(zdbID) ;
    }



    private void initGUI(){
        frame = new Frame() ;
        frame.setWidth("400");
        frame.setHeight("600");
        frame.setStylePrimaryName("gwt-Frame");
        setWidget(frame) ;
    }



    public void showPopup(String zdbID){
        String url = BASE_URL + zdbID ;
        setPopupPositionAndShow(new PopupPanel.PositionCallback(){
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 2;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
                setPopupPosition(left, top);
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        frame.setUrl(url);
    }

}
