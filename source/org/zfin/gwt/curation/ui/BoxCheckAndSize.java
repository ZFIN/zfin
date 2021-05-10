package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.zfin.gwt.root.dto.CuratorSessionDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.SessionSaveService;
import org.zfin.gwt.root.ui.SessionSaveServiceAsync;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.Collection;
import java.util.HashSet;

/**
 * Control of a box in size and check box options.
 */
public class BoxCheckAndSize extends HorizontalPanel {

    private boolean isCheckOn;
    private String publicationID;

    private Collection<ClickHandler> checkAllHandlerList = new HashSet<ClickHandler>(2);
    private Collection<ClickHandler> checkNoneHandlerList = new HashSet<ClickHandler>(2);

    // The box that should be re-sized.
    private Widget resizeBox;
    private static final String ALL = "all";
    private static final String NONE = "none";

    private SessionSaveServiceAsync sessionSaveServiceAsync = SessionSaveService.App.getInstance();

    public BoxCheckAndSize(boolean checkOn, Widget box, String publicationID) {
        isCheckOn = checkOn;
        this.resizeBox = box;
        this.publicationID = publicationID;
        initGui();
    }

    private void initGui() {
        if (isCheckOn) {
            initCheckBoxOptions();
        }
        initResizeOptions();
    }

    public void initializeSessionVariables() {
        String message = "Error while retrieving session variable";
        sessionSaveServiceAsync.readBoxSizeFromSession(publicationID, resizeBox.getElement().getId(), new RetrieveSessionVariableCallback(message, null));
    }

    @SuppressWarnings({"FeatureEnvy"})
    private void initResizeOptions() {
        Hyperlink small = new Hyperlink(BoxSize.SMALL.getName(), BoxSize.SMALL.getName());
        small.addClickHandler(new ResizeBoxClickHandler(BoxSize.SMALL));
        Hyperlink medium = new Hyperlink(BoxSize.MEDIUM.getName(), BoxSize.MEDIUM.getName());
        medium.addClickHandler(new ResizeBoxClickHandler(BoxSize.MEDIUM));
        Hyperlink large = new Hyperlink(BoxSize.LARGE.getName(), BoxSize.LARGE.getName());
        large.addClickHandler(new ResizeBoxClickHandler(BoxSize.LARGE));
        Hyperlink fitted = new Hyperlink(BoxSize.FITTED.getName(), BoxSize.FITTED.getName());
        fitted.addClickHandler(new ResizeBoxClickHandler(BoxSize.FITTED));
        HorizontalPanel size = new HorizontalPanel();
        size.add(new Label("size: "));
        size.add(small);
        size.add(WidgetUtil.getNbsp());
        size.add(medium);
        size.add(WidgetUtil.getNbsp());
        size.add(large);
        size.add(WidgetUtil.getNbsp());
        size.add(fitted);
        add(size);
        WidgetUtil.addOrRemoveCssStyle(size, WidgetUtil.RIGHT_ALIGN_BOX, true);
        WidgetUtil.addOrRemoveCssStyle(size, WidgetUtil.CSS_CURATION_BOX_CONTROL_TEXT, true);
        setStyleName(WidgetUtil.RIGHT_ALIGN_BOX);
    }

    private void initCheckBoxOptions() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new Label("check: "));
        Hyperlink checkAll = new Hyperlink(ALL, ALL);
        checkAll.addClickHandler(new CheckRecordsClickHandler(true));
        panel.add(checkAll);
        panel.add(WidgetUtil.getNbsp());
        Hyperlink checkNone = new Hyperlink(NONE, NONE);
        checkNone.addClickHandler(new CheckRecordsClickHandler(false));
        panel.add(checkNone);
        add(panel);
        WidgetUtil.addOrRemoveCssStyle(panel, WidgetUtil.CSS_CURATION_BOX_CONTROL_TEXT, true);
    }

    public void addCheckAllClickHandler(ClickHandler handler) {
        checkAllHandlerList.add(handler);
    }

    public void addUnSelectAllClickHandler(ClickHandler handler) {
        checkNoneHandlerList.add(handler);
    }


    private enum BoxSize {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large"),
        FITTED("fitted");

        private String value;

        BoxSize(String value) {
            this.value = value;
        }

        public String getName() {
            return value;
        }
    }

    private class ResizeBoxClickHandler implements ClickHandler {

        private BoxSize size;

        public ResizeBoxClickHandler(BoxSize boxSize) {
            size = boxSize;
        }

        public void onClick(ClickEvent clickEvent) {
            resizeBox.setStyleName(getCssClassName(size));
            //Window.alert("hello");
            sessionSaveServiceAsync.updateCuratorSession(createCuratorSession(), new AsyncCallback() {
                public void onFailure(Throwable throwable) {

                }

                public void onSuccess(Object o) {

                }
            });
        }

        private CuratorSessionDTO createCuratorSession() {
            CuratorSessionDTO curatorSessionUpdate = new CuratorSessionDTO();
            curatorSessionUpdate.setPublicationZdbID(publicationID);
            //Window.alert(resizeBox.getElement().getId());
            curatorSessionUpdate.setField(resizeBox.getElement().getId());
            curatorSessionUpdate.setValue(getCssClassName(size));
            return curatorSessionUpdate;
        }

        private String getCssClassName(BoxSize boxSize) {
            return boxSize.getName() + "-curation-box";
        }
    }

    private class CheckRecordsClickHandler implements ClickHandler {

        private boolean checkAllRecords;

        private CheckRecordsClickHandler(boolean checkAll) {
            this.checkAllRecords = checkAll;
        }

        public void onClick(ClickEvent clickEvent) {
            if (checkAllRecords) {
                for (ClickHandler handler : checkAllHandlerList)
                    handler.onClick(clickEvent);
            } else {
                for (ClickHandler handler : checkNoneHandlerList)
                    handler.onClick(clickEvent);
            }
        }
    }

    class RetrieveSessionVariableCallback extends ZfinAsyncCallback<CuratorSessionDTO> {

        RetrieveSessionVariableCallback(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        protected void onFailureCleanup() {

        }

        public void onSuccess(CuratorSessionDTO curatorSessionDTO) {
            if (curatorSessionDTO == null)
                return;
            //Window.alert(curatorSessionDTO.getValue());
            resizeBox.setStyleName(curatorSessionDTO.getValue());

        }
    }
}
