package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.LookupRPCService;

import java.util.List;

/**
 */
public class AttributionModule extends AbstractRevertibleComposite<RelatedEntityDTO> {

    private HorizontalPanel container = new HorizontalPanel();
    private LookupComposite markerLookupComposite = new LookupComposite();
    private LookupComposite featureLookupComposite = new LookupComposite();
    private ListBoxWrapper removeListBox = new ListBoxWrapper(false);
    private HTML messageBox = new HTML("");

    public static enum RemoveHeader {
        MARKER,
        FEATURE,
        GENOTYPE,;

        private static final String stars = "**";

        public String toString() {
            return stars + name();
        }

        public static boolean isHeader(String value) {
            return (value.startsWith(stars));
        }
    }

    public AttributionModule() {
        this(StandardDivNames.directAttributionDiv);
    }

    public AttributionModule(String div) {
        initWidget(panel);
        initGUI();
        setValues();
        addInternalListeners(this);
        if (div != null) {
            RootPanel.get(div).add(this);
        }
    }


    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    protected void revertGUI() {
        working();
        LookupRPCService.App.getInstance().getAttributionsForPub(dto.getPublicationZdbID(),
                new MarkerEditCallBack<List<RelatedEntityDTO>>("Failed to return attributions for: " + dto, this) {
                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(List<RelatedEntityDTO> result) {
                        removeListBox.clear();
                        for (RelatedEntityDTO relatedEntityDTO : result) {
                            removeListBox.addItem(relatedEntityDTO.getName(), relatedEntityDTO.getZdbID());
                        }
                        notWorking();
                    }
                });
        markerLookupComposite.clearNote();
        markerLookupComposite.clearError();
        featureLookupComposite.clearNote();
        featureLookupComposite.clearError();
        clearMessage();
    }

    @Override
    protected void initGUI() {
        markerLookupComposite.setType(LookupComposite.MARKER_LOOKUP);
        markerLookupComposite.setButtonText("Attr Marker");
        markerLookupComposite.setWildCard(false);
        markerLookupComposite.setAction(new SubmitAction() {
            @Override
            public void doSubmit(String value) {
                addMarkerAttribution(value);
            }
        });
        markerLookupComposite.initGui();
        container.add(markerLookupComposite);
        container.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

        featureLookupComposite.setType(LookupComposite.FEATURE_LOOKUP);
        featureLookupComposite.setButtonText("Attr Feature");
        featureLookupComposite.setWildCard(false);
        featureLookupComposite.setAction(new SubmitAction() {
            @Override
            public void doSubmit(String value) {
                addFeatureAttribution(value);
            }
        });
        featureLookupComposite.initGui();
        container.add(featureLookupComposite);
        container.add(new HTML("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"));

        container.add(new Label("Remove Attr"));
        container.add(removeListBox);
        panel.add(container);
        errorLabel.setStyleName("error");
        panel.add(errorLabel);

        messageBox.setStyleName("clickable");
        panel.add(messageBox);
    }

    public void setMessage(String message) {
        messageBox.setHTML(message + " [close]");
    }

    public void clearMessage() {
        messageBox.setHTML("");
    }

    @Override
    protected void setValues() {

    }

    @Override
    protected void addInternalListeners(final HandlesError handlesError) {

        messageBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearMessage();
            }
        });

        removeListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                final String attributionToRemoveID = removeListBox.getSelectedStringValue();
                final String attributionToRemoveLabel = removeListBox.getSelectedText();
                if (RemoveHeader.isHeader(attributionToRemoveID)) {
                    return;
                }

                if(false==Window.confirm("Are you sure you want to delete: "+ attributionToRemoveLabel)){
                    return ;
                }


                working();
                MarkerRPCService.App.getInstance().removeAttribution(attributionToRemoveID, dto.getPublicationZdbID(),
                        new MarkerEditCallBack<String>("Failed to remove attribution: " + attributionToRemoveLabel, handlesError) {

                            @Override
                            public void onFailure(Throwable throwable) {
                                super.onFailure(throwable);
                                notWorking();
                            }

                            @Override
                            public void onSuccess(String message) {
                                notWorking();
                                if (message == null) {
                                    fireEventSuccess();
                                    clearError();
                                    setMessage("Removed attribution: " + attributionToRemoveLabel);
                                    resetInput();
                                } else if (message != null && false == message.isEmpty()) {
                                    setError(message);
                                    clearMessage();
                                }
                            }
                        });

            }
        });
    }

    private void addMarkerAttribution(final String value) {
        if (checkAttributionExists(value)) return;
        working();
        MarkerRPCService.App.getInstance().addAttributionForMarkerName(value, dto.getPublicationZdbID(),
                new MarkerEditCallBack<Void>("Failed to add attribution for: " + value, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        notWorking();
                        fireEventSuccess();
                        clearError();
                        setMessage("Marker attribution added: " + value);
                        resetInput();
                    }
                });
    }

    private boolean checkAttributionExists(String attribution) {
        if (removeListBox.setIndexForValue(attribution) >= 0) {
            setError("'" + attribution + "' is already attributed.");
            return true;
        }
        return false;
    }

    private void addFeatureAttribution(final String value) {
        if (checkAttributionExists(value)) return;
        working();
        MarkerRPCService.App.getInstance().addAttributionForFeatureName(value, dto.getPublicationZdbID(),
                new MarkerEditCallBack<Void>("Failed to add attribution for: " + value, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        notWorking();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        notWorking();
                        fireEventSuccess();
                        clearError();
                        setMessage("Feature attribution added:" + value);
                        resetInput();
                    }
                });
    }

    public void setPublication(String publicationID) {
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        setDTO(relatedEntityDTO);
    }

    private void resetInput() {
        markerLookupComposite.setText("");
        featureLookupComposite.setText("");
    }

    @Override
    public void working() {
        super.working();
        markerLookupComposite.working();
        featureLookupComposite.working();
        removeListBox.setEnabled(false);
    }

    @Override
    public void notWorking() {
        super.notWorking();
        markerLookupComposite.notWorking();
        featureLookupComposite.notWorking();
        removeListBox.setEnabled(true);
    }
}
