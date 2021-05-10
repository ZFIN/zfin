package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;
import org.zfin.gwt.root.ui.*;

/**
 */
public class PreviousNamesBox extends RelatedEntityBox {


    public PreviousNamesBox() {
        super();
        addInternalListeners(this);
        RootPanel.get(StandardDivNames.previousNameDiv).add(this);
    }


    void addInternalListeners(final HandlesError handlesError) {
        addRelatedEntityCompositeListener(new RelatedEntityListener<RelatedEntityDTO>() {
            public void addRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> event) {

                MarkerRPCService.App.getInstance().addDataAliasRelatedEntity(event.getDTO(),
                        new MarkerEditCallBack<RelatedEntityDTO>("Failed to add name: ", handlesError) {
                            public void onSuccess(RelatedEntityDTO relatedEntityDTO) {
                                addRelatedEntityToGUI(relatedEntityDTO);
                            }
                        });

            }

            public void addAttribution(RelatedEntityEvent<RelatedEntityDTO> event) {
                MarkerRPCService.App.getInstance().addDataAliasAttribution(event.getDTO(),
                        new MarkerEditCallBack<RelatedEntityDTO>("failed to add attribution: ", handlesError) {
                            public void onSuccess(RelatedEntityDTO relatedEntityDTO) {
                                addAttributionToGUI(relatedEntityDTO);
                            }
                        });

            }

            public void removeRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> event) {
                final RelatedEntityDTO dto = event.getDTO();
                MarkerRPCService.App.getInstance().removeDataAliasRelatedEntity(event.getDTO(),
                        new MarkerEditCallBack<Void>("failed to remove name: ", handlesError) {
                            public void onSuccess(Void o) {
                                removeRelatedEntityFromGUI(dto);
                            }
                        });
            }

            public void removeAttribution(RelatedEntityEvent<RelatedEntityDTO> event) {
                final RelatedEntityDTO dto = event.getDTO();
                MarkerRPCService.App.getInstance().removeDataAliasAttribution(event.getDTO(),
                        new MarkerEditCallBack<Void>("failed to remove attribution: ", handlesError) {
                            public void onSuccess(Void o) {
                                removeAttributionFromGUI(dto);
                            }
                        });
            }
        });

    }


}