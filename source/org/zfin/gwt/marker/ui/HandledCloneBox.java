package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.CloneDataChangedEvent;
import org.zfin.gwt.marker.event.CloneDataListener;
import org.zfin.gwt.root.dto.CloneDTO;

/**
 */
public final class HandledCloneBox extends CloneBox {


    public HandledCloneBox(String div) {
        super();
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    protected void addInternalListeners(final CloneBox cloneBox) {
        addCloneDataListener(new CloneDataListener() {
            public void cloneDataChanged(CloneDataChangedEvent cloneDataChangedEvent) {
                // on success
                final CloneDTO returnCloneDTO = cloneDataChangedEvent.getCloneDTO();
                returnCloneDTO.setZdbID(cloneBox.getCloneDTO().getZdbID());
                CloneRPCService.App.getInstance().updateCloneData(returnCloneDTO, new MarkerEditCallBack<CloneDTO>("Failed to update clone: ") {
                    public void onSuccess(CloneDTO updatedDTO) {
                        cloneBox.setDomain(updatedDTO);
                        cloneBox.refreshGUI();
                        clearError();
                    }
                });
            }
        });

    }
}