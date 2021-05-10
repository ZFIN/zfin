package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.PopupPanel;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.root.ui.OrderedList;

import java.util.Date;

/**
 * Entry point for history panel.
 */
public class HistoryModule {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("HistoryModule.ui.xml")
    interface MyUiBinder extends UiBinder<PopupPanel, HistoryModule> {
    }

    @UiField
    Grid dataTable;

    PopupPanel popupPanel;

    public HistoryModule() {
        init();
    }

    public void init() {
        popupPanel = uiBinder.createAndBindUi(this);
        popupPanel.setAutoHideEnabled(true);
        popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
                int left = (Window.getClientWidth() - offsetWidth) / 3;
                int top = (Window.getClientHeight() - offsetHeight) / 3;
                popupPanel.setPopupPosition(left, top);
            }
        });
        popupPanel.hide();
        popupPanel.setStyleName("history-popup-panel");
    }

    public void addItem(CurationEvent event) {
        int row = dataTable.getRowCount() + 1;
        dataTable.resizeRows(row);
        Date now = new Date();
        dataTable.setText(row - 1, 0, DateTimeFormat.getFormat("HH:mm:ss").format(now));
        dataTable.setText(row - 1, 1, event.eventType.name());
        dataTable.setText(row - 1, 2, event.getDescription());
    }

}
