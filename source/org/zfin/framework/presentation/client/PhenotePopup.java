package org.zfin.framework.presentation.client;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.*;

/**
 */
public class PhenotePopup extends PopupPanel {

    private final String BASE_URL =  "/action/anatomy/term-info?anatomyItem.zdbID=" ;
    private Frame frame ;
    private int  height = 600 ; 
    private int  width = 400 ; 

    public PhenotePopup(){
        super(true,true) ;
//        super(false,false) ;
        initGUI();
    }

    public PhenotePopup(String zdbID){
        this() ;
        showPopup(zdbID) ;
    }



    private void initGUI(){
        frame = new Frame() ;
        frame.setHeight(""+height);
        frame.setWidth(""+width);
        frame.setStylePrimaryName("gwt-Frame");
        setWidget(frame) ;
    }



    public void showPopup(String zdbID){
        String url = BASE_URL + zdbID ;
        center() ; 
        // the  offset width/height is the same as that of the frame
//        setPopupPositionAndShow(new PopupPanel.PositionCallback(){
//            public void setPosition(int offsetWidth, int offsetHeight) {
//                int left =  (Window.getClientWidth()+Window.getScrollLeft()-offsetWidth)/2 ; 
//                int top =  (Window.getClientHeight()+Window.getScrollTop()-offsetHeight) ; 
//                setPopupPosition(left, top);
//                //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });

        frame.setUrl(url);
    }

}
