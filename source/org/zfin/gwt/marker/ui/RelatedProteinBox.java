package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.MarkerLoadEvent;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.marker.event.RelatedEntityListener;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;

/**
 */
public class RelatedProteinBox extends RelatedEntityBox {


    public RelatedProteinBox(String div) {
        super();
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    public void addInternalListeners(final RelatedEntityBox relatedEntityBox) {
        addRelatedEntityCompositeListener(new RelatedEntityListener<RelatedEntityDTO>() {
            public void addRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> relatedEntityEvent) {
                final RelatedEntityDTO dto = relatedEntityEvent.getRelatedEntityDTO();
                dto.setDataZdbID(getZdbID());
                TranscriptRPCService.App.getInstance().addProteinRelatedEntity(dto,
                        new MarkerEditCallBack<DBLinkDTO>("Failed to add protein: ", relatedEntityBox) {
                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                addRelatedEntityToGUI(dbLinkDTO);
                            }
                        });

            }

            public void addAttribution(RelatedEntityEvent<RelatedEntityDTO> relatedEntityEvent) {
                final DBLinkDTO dto = (DBLinkDTO) relatedEntityEvent.getRelatedEntityDTO();
                dto.setDataZdbID(getZdbID());
                TranscriptRPCService.App.getInstance().addProteinAttribution(dto,
                        new MarkerEditCallBack<DBLinkDTO>("failed to add attribution: ", relatedEntityBox) {
                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                addAttributionToGUI(dbLinkDTO);
                            }
                        });
            }

            public void removeRelatedEntity(RelatedEntityEvent<RelatedEntityDTO> relatedEntityEvent) {
                final DBLinkDTO dto = (DBLinkDTO) relatedEntityEvent.getRelatedEntityDTO();
                dto.setDataZdbID(getZdbID());
                TranscriptRPCService.App.getInstance().removeProteinRelatedEntity(dto,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove protein: ", relatedEntityBox) {
                            public void onSuccess(DBLinkDTO o) {
                                removeRelatedEntityFromGUI(o);
                            }
                        });

            }

            public void removeAttribution(RelatedEntityEvent<RelatedEntityDTO> relatedEntityEvent) {
                final DBLinkDTO dto = (DBLinkDTO) relatedEntityEvent.getRelatedEntityDTO();
                dto.setDataZdbID(getZdbID());
                TranscriptRPCService.App.getInstance().removeProteinAttribution(dto,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove attribution: ", relatedEntityBox) {
                            public void onSuccess(DBLinkDTO o) {
                                removeAttributionFromGUI(o);
                            }
                        });

            }
        });
    }

    public void markerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
        setRelatedEntities(markerLoadEvent.getMarkerDTO().getZdbID(),
                markerLoadEvent.getMarkerDTO().getRelatedProteinAttributes());
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        return relatedEntityDTO.getName().startsWith("ZFINPROT");
    }
}

