package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.marker.presentation.dto.CloneDTO;
import org.zfin.marker.presentation.event.CloneDataChangedEvent;
import org.zfin.marker.presentation.event.CloneDataListener;

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
                if (returnCloneDTO.getProblemType() != null &&
                        ((false == cloneBox.getCloneDTO().getZdbID().startsWith("ZDB-EST"))
                                &&
                                (false == cloneBox.getCloneDTO().getZdbID().startsWith("ZDB-CDNA")))
                        ) {
                    cloneBox.setError("Only EST's and CDNA's can have problem types.");
                    return;
                }
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