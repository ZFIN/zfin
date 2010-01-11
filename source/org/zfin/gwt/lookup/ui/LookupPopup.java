package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import org.zfin.gwt.root.ui.LookupComposite;

/**
 */
public class LookupPopup extends PopupPanel {

    private final String ANATOMY_BASE_URL = "/action/anatomy/term-info?anatomyItem.zdbID=";
    private final String MARKER_BASE_URL = "/action/marker/mini-gene?zdbID=";
    private Frame frame;
    private int height = 600;
    private int width = 400;
    private String type;

    public LookupPopup(String type) {
        super(true, true);
        this.type = type;
//        super(false,false) ;
        initGUI();
    }

    public LookupPopup(String type, String zdbID) {
        this(type);
        showPopup(zdbID);
    }


    private void initGUI() {
        frame = new Frame();
        frame.setHeight("" + height);
        frame.setWidth("" + width);
        frame.setStylePrimaryName("gwt-Frame");
        setWidget(frame);
    }


    public void showPopup(String zdbID) {
        String url;
        if (type.equals(LookupComposite.MARKER_LOOKUP)) {
            url = MARKER_BASE_URL + zdbID;
        } else {
            url = ANATOMY_BASE_URL + zdbID;
        }

        center();
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
