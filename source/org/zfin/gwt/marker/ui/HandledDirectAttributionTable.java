package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.event.DirectAttributionListener;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerRPCService;
import org.zfin.gwt.root.ui.StandardDivNames;

class HandledDirectAttributionTable extends DirectAttributionTable {

    public HandledDirectAttributionTable() {
        super();
        addInternalListeners(this);
        RootPanel.get(StandardDivNames.directAttributionDiv).add(this);
    }

    void addInternalListeners(final DirectAttributionTable directAttributionTable) {
        addDirectAttributionListener(new DirectAttributionListener() {
            public void remove(final String pubZdbID) {
                MarkerRPCService.App.getInstance().removeAttribution(getZdbID(), pubZdbID,
                        new MarkerEditCallBack<String>("failed to remove a publication: ", directAttributionTable) {
                            public void onSuccess(String o) {
                                removeReferenceFromGUI(pubZdbID);
                            }
                        });
            }

            public void add(final String pubZdbID) {
                MarkerRPCService.App.getInstance().addAttribution(getZdbID(), pubZdbID,
                        new MarkerEditCallBack<Void>("failed to add a publication: ", directAttributionTable) {
                            public void onSuccess(Void o) {
                                addPublicationToGUI(pubZdbID);
                            }
                        });
            }
        });


    }


}