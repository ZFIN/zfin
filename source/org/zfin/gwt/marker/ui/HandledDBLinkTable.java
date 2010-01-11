package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.DBLinkTableEvent;
import org.zfin.gwt.marker.event.DBLinkTableListener;
import org.zfin.gwt.marker.event.RelatedEntityEvent;
import org.zfin.gwt.marker.event.RelatedEntityListener;
import org.zfin.gwt.root.dto.DBLinkDTO;


/**
 * This class simply has handles.
 */
public class HandledDBLinkTable extends DBLinkTable {


    public HandledDBLinkTable(String div) {
        super();
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }


    public void addInternalListeners(final DBLinkTable dbLinkTable) {

        addRelatedEntityCompositeListener(new RelatedEntityListener<DBLinkDTO>() {
            public void addRelatedEntity(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
                final DBLinkDTO dbLinkDTO = dbLinkDTORelatedEntityEvent.getRelatedEntityDTO();
                dbLinkDTO.setDataZdbID(getZdbID());

                // validate data
                MarkerRPCService.App.getInstance().validateDBLink(dbLinkDTO,
                        new MarkerEditCallBack<String>("validated dblink", dbLinkTable) {
                            public void onSuccess(String warning) {

                                boolean confirmed = true;
                                if (warning != null) {
                                    confirmed = Window.confirm(warning + "\nContinue?");
                                }

                                if (true == confirmed) {
                                    MarkerRPCService.App.getInstance().addDBLink(dbLinkDTO, getReferenceDatabases(),
                                            new MarkerEditCallBack<DBLinkDTO>("failed to add dblink: ", dbLinkTable) {
                                                public void onSuccess(DBLinkDTO dbLinkDTO) {
                                                    addRelatedEntityToGUI(dbLinkDTO);
                                                    resetInput();
                                                }
                                            });
                                }
                            }
                        });


            }

            public void addAttribution(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
                final DBLinkDTO dbLinkDTO = dbLinkDTORelatedEntityEvent.getRelatedEntityDTO();
                dbLinkDTO.setDataZdbID(getZdbID());
                // don't think that we need this, hopefully
                MarkerRPCService.App.getInstance().addDBLinkAttribution(dbLinkDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to add dblink reference: ", dbLinkTable) {
                            public void onSuccess(DBLinkDTO o) {
                                addAttributionToGUI(o);
                                resetInput();
                            }
                        });
            }

            public void removeRelatedEntity(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
                final DBLinkDTO dbLinkDTO = dbLinkDTORelatedEntityEvent.getRelatedEntityDTO();
                dbLinkDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().removeDBLink(dbLinkDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove dblink: ", dbLinkTable) {
                            public void onSuccess(DBLinkDTO o) {
                                removeRelatedEntityFromGUI(o);
                            }
                        });
            }

            public void removeAttribution(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
                final DBLinkDTO dbLinkDTO = dbLinkDTORelatedEntityEvent.getRelatedEntityDTO();
                dbLinkDTO.setDataZdbID(getZdbID());
                MarkerRPCService.App.getInstance().removeDBLinkAttribution(dbLinkDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to remove dblink reference: ", dbLinkTable) {
                            public void onSuccess(DBLinkDTO o) {
                                removeAttributionFromGUI(o);
                            }
                        });
            }
        });

        addDBLinkTableListener(new DBLinkTableListener() {
            public void updateDBLink(DBLinkTableEvent dbLinkTableEvent) {
                final DBLinkDTO dbLinkDTO = dbLinkTableEvent.getDBLinkDTO();
                MarkerRPCService.App.getInstance().updateDBLinkLength(dbLinkDTO,
                        new MarkerEditCallBack<DBLinkDTO>("failed to update dblink attributes: ", dbLinkTable) {
                            public void onSuccess(DBLinkDTO dbLinkDTO) {
                                updatedLength(dbLinkDTO);
                            }

                        });
            }
        });
    }

}