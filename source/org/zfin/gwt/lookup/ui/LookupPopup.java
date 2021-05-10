package org.zfin.gwt.lookup.ui;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import org.zfin.framework.presentation.LookupStrings;

/**
 */
public class LookupPopup extends PopupPanel {

    private final String MARKER_BASE_URL = "/action/marker/mini-gene?zdbID=";
    private final String ANATOMY_BASE_URL = "/action/ontology/term-detail-popup-button?termID=";
    private Frame frame;
    private String type;


    public LookupPopup(String type) {
        super(true, true);
        this.type = type;
//        super(false,false) ;
        initGUI();
    }

    public LookupPopup(String type, String zdbID) {
        this(type);
        //String oboID=RepositoryFactory.getInfrastructureRepository().getTermByID(zdbID).getOboID();
        showPopup(zdbID);
    }


    private void initGUI() {
        frame = new Frame();
        frame.setStylePrimaryName("gwt-Frame");
        frame.addStyleName("xpatselect-anatomy-popup");
        setWidget(frame);
    }


    public void showPopup(String zdbID) {
        String url;
        if (type.equals(LookupStrings.MARKER_LOOKUP)) {
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
