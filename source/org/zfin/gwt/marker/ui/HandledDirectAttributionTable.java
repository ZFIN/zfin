package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.DirectAttributionEvent;
import org.zfin.gwt.marker.event.DirectAttributionListener;

public class HandledDirectAttributionTable extends DirectAttributionTable {

    public HandledDirectAttributionTable(String div) {
        super();
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    public void addInternalListeners(final DirectAttributionTable directAttributionTable) {
        addDirectAttributionListener(new DirectAttributionListener() {
            public void remove(final DirectAttributionEvent directAttributionEvent) {
                MarkerRPCService.App.getInstance().removeMarkerAttribution(getZdbID(), directAttributionEvent.getPubZdbID(),
                        new MarkerEditCallBack<Void>("failed to remove a publication: ", directAttributionTable) {
                            public void onSuccess(Void o) {
                                removeReferenceFromGUI(directAttributionEvent.getPubZdbID());
                            }
                        });
            }

            public void add(final DirectAttributionEvent directAttributionEvent) {
                MarkerRPCService.App.getInstance().addMarkerAttribution(getZdbID(), directAttributionEvent.getPubZdbID(),
                        new MarkerEditCallBack<Void>("failed to add a publication: ", directAttributionTable) {
                            public void onSuccess(Void o) {
                                addPublicationToGUI(directAttributionEvent.getPubZdbID());
                            }
                        });
            }
        });


    }


}