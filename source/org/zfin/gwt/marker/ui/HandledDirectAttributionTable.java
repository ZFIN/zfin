package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.DirectAttributionListener;

class HandledDirectAttributionTable extends DirectAttributionTable {

    public HandledDirectAttributionTable() {
        super();
        addInternalListeners(this);
        RootPanel.get(StandardMarkerDivNames.directAttributionDiv).add(this);
    }

    void addInternalListeners(final DirectAttributionTable directAttributionTable) {
        addDirectAttributionListener(new DirectAttributionListener() {
            public void remove(final String pubZdbID) {
                MarkerRPCService.App.getInstance().removeMarkerAttribution(getZdbID(), pubZdbID,
                        new MarkerEditCallBack<Void>("failed to remove a publication: ", directAttributionTable) {
                            public void onSuccess(Void o) {
                                removeReferenceFromGUI(pubZdbID);
                            }
                        });
            }

            public void add(final String pubZdbID) {
                MarkerRPCService.App.getInstance().addMarkerAttribution(getZdbID(), pubZdbID,
                        new MarkerEditCallBack<Void>("failed to add a publication: ", directAttributionTable) {
                            public void onSuccess(Void o) {
                                addPublicationToGUI(pubZdbID);
                            }
                        });
            }
        });


    }


}