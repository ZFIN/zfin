package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.curation.ui.FeatureServiceGWT;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ValidationException;
import org.zfin.gwt.root.util.AppUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FeatureRelationshipPresenter implements HandlesError {

    public static final String ALL = "All";
    private FeatureRelationshipView view;
    private String publicationID;

    // internal data
    private List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOs;
    private List<FeatureDTO> featureDTOs;
    private String lastSelectedFeatureZdbId = null;

    private String featureNameFilter;
    private String featureTypeFilter;

    public FeatureRelationshipPresenter(FeatureRelationshipView view, String publicationID) {
        this.publicationID = publicationID;
        this.view = view;
    }

    public void go() {
        loadValues();
    }

    protected void populateDataTable() {
        int elementIndex = 0;
        FeatureDTO pastFeature = null;
        view.emptyDataTable();
        for (FeatureMarkerRelationshipDTO relationshipDTO : featureMarkerRelationshipDTOs) {
            if (featureNameFilter != null && !relationshipDTO.getFeatureDTO().getAbbreviation().equals(featureNameFilter))
                continue;
            if (featureTypeFilter != null && !relationshipDTO.getFeatureDTO().getFeatureType().getDisplay().equals(featureTypeFilter))
                continue;
            FeatureDTO feature = relationshipDTO.getFeatureDTO();
            view.addFeatureCell(feature, pastFeature, elementIndex);
            view.addFeatureTypeCell(relationshipDTO.getFeatureDTO(), elementIndex);
            view.addFeatureRelationshipCell(relationshipDTO.getRelationshipType(), elementIndex);
            view.addTargetMarker(relationshipDTO.getMarkerDTO(), elementIndex);
            view.addDeletButton(new DeleteFeatureMarkerRelationshipButton(relationshipDTO, this), elementIndex);
            elementIndex++;
            pastFeature = feature;
        }
        view.endTableUpdate();
    }

    private void loadValues() {
        loadValues(false);
    }

    private void loadValues(boolean forcedLoad) {
        // retrieve Filter elements
        loadFilterValues(forcedLoad);

        // get Feature-Marker-Relationships
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_MARKER_RELATIONSHIPS_FOR_PUB_START);
        FeatureRPCService.App.getInstance().getFeatureMarkerRelationshipsForPub(publicationID,
                new FeatureEditCallBack<List<FeatureMarkerRelationshipDTO>>(
                        "Failed to find feature marker relationships for this pub: "
                                + publicationID, this) {
                    @Override
                    public void onSuccess(List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOList) {
                        featureMarkerRelationshipDTOs = featureMarkerRelationshipDTOList;
                        populateDataTable();
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_MARKER_RELATIONSHIPS_FOR_PUB_STOP);
                    }
                });

        // collect features for pub
        loadFeatureList();

    }

    private void loadFeatureList() {
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_START);
        FeatureServiceGWT.getFeatureList(publicationID,
                new FeatureEditCallBack<List<FeatureDTO>>("Problem finding features for pub: " + publicationID + " ", this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.featureList.setEnabled(false);
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }

                    @Override
                    public void onSuccess(List<FeatureDTO> features) {
                        featureDTOs = features;
                        if (featureDTOs != null) {
                            view.featureList.clear();
                            view.featureList.addItem("-----------");
                            for (FeatureDTO featureDTO : featureDTOs) {
                                view.featureList.addItem(featureDTO.getName(), featureDTO.getZdbID());
                            }
                            view.featureList.setEnabled(true);
                        }

                        if (lastSelectedFeatureZdbId != null) {
                            view.featureList.setIndexForValue(lastSelectedFeatureZdbId);
                            lastSelectedFeatureZdbId = null;
                        }
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }
                });
    }

    private void loadFilterValues(boolean forceLoad) {
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_START);
        FeatureServiceGWT.getFeatureList(publicationID,
                new FeatureEditCallBack<List<FeatureDTO>>("Problem finding features for pub: " + publicationID + " ", this) {
                    @Override
                    public void onSuccess(List<FeatureDTO> featureMarkerRelationshipList) {
                        if (featureMarkerRelationshipList != null) {
                            Map<String, FeatureTypeEnum> featureTypes = new TreeMap<>();
                            view.featureNameList.clear();
                            view.featureNameList.addItem(ALL, (String) null);
                            for (FeatureDTO featureDTO : featureMarkerRelationshipList) {
                                view.featureNameList.addItem(featureDTO.getName(), featureDTO.getZdbID());
                                featureTypes.put(featureDTO.getFeatureType().getDisplay(), featureDTO.getFeatureType());
                            }

                            view.featureTypeList.clear();
                            view.featureTypeList.addItem(ALL, (String) null);
                            for (String type : featureTypes.keySet()) {
                                view.featureTypeList.addItem(type, type);
                            }
                        }
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_FEATURE_LIST_STOP);
                    }
                }, forceLoad);
    }

    @Override
    public void setError(String message) {
        view.errorLabel.setError(message);
    }

    @Override
    public void clearError() {

    }

    @Override
    public void fireEventSuccess() {

    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {

    }

    private FeatureDTO selectedFeatureDTO;

    public void onFeatureSelectionChange(String selectedText) {
        selectedFeatureDTO = getFeatureDTOForName(selectedText);
        if (selectedFeatureDTO != null && selectedFeatureDTO.getFeatureType() != null) {
            view.featureType.setText(selectedFeatureDTO.getFeatureType().getDisplay());
        } else {
            setError("Feature type was null");
            return;
        }
        loadRelationshipTypes();

    }

    public void loadRelationshipTypes() {
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_RELATIONSHIP_TYPES_FOR_FEATURE_TYPE_START);
        FeatureRPCService.App.getInstance().getRelationshipTypesForFeatureType(selectedFeatureDTO.getFeatureType(),
                new FeatureEditCallBack<List<String>>("Failed to return feature relationships for type: " + selectedFeatureDTO.getFeatureType().getDisplay(), this) {
                    @Override
                    public void onSuccess(List<String> result) {
                        view.relationshipList.clear();
                        if (result != null && result.size() > 0) {
                            if (result.size() == 1) {
                                // this is probably correct so we don't need to screen it
                                view.relationshipList.addItem(result.get(0));
                            } else {
                                view.relationshipList.addItem("-------");
                                for (String rel : result) {
                                    // see case 6337
                                    // is_allele relationship should only be available for transgenic insertions where the known insertion site box is checked
                                    // unspecified transgenic will never have known insertion sites
                                    if ((selectedFeatureDTO.getFeatureType() == FeatureTypeEnum.TRANSGENIC_INSERTION
                                    )
                                            && rel.startsWith("is allele of")) {
                                        if (selectedFeatureDTO.getKnownInsertionSite()) {
                                            view.relationshipList.addItem(rel);
                                        }
                                    } else {
                                        view.relationshipList.addItem(rel);
                                    }
                                }
                            }
                            view.relationshipList.setEnabled(true);
                        }
                        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_RELATIONSHIP_TYPES_FOR_FEATURE_TYPE_STOP);
                    }
                }
        );
    }

    protected void updateTargetGeneList(String selectedFeature, final String selectedRelationship) {
        view.addButton.setEnabled(false);
        view.targetMarkerList.setEnabled(false);
        final String mutagenForFeature = getFeatureDTOForName(selectedFeature).getMutagen();
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_RELATIONSHIP_TYPES_FOR_FEATURE_TYPE_START);
        FeatureRPCService.App.getInstance().getMarkersForFeatureRelationAndSource(selectedRelationship, publicationID,
                new FeatureEditCallBack<List<MarkerDTO>>("Failed to find markers for type[" + view.featureType.getText() + "] and pub: " +
                        publicationID, this) {
                    @Override
                    public void onSuccess(List<MarkerDTO> markers) {
                        view.targetMarkerList.clear();
                        Collections.sort(markers);
                        if (markers.size() > 0) {
                            for (MarkerDTO m : markers) {
                                if (mutagenForFeature != null) {
                                    if (selectedRelationship.equalsIgnoreCase("created by")) {
                                        if (m.getName().startsWith(mutagenForFeature)) {
                                            view.targetMarkerList.addItem(m.getName(), m.getZdbID());
                                        } else if (mutagenForFeature.equals("DNA and CRISPR") && m.getName().startsWith("CRISPR")) {
                                            view.targetMarkerList.addItem(m.getName(), m.getZdbID());
                                        } else if (mutagenForFeature.equals("DNA and TALEN") && m.getName().startsWith("TALEN")) {
                                            view.targetMarkerList.addItem(m.getName(), m.getZdbID());
                                        }
                                    } else {
                                        view.targetMarkerList.addItem(m.getName(), m.getZdbID());
                                    }
                                } else {
                                    view.targetMarkerList.addItem(m.getName(), m.getZdbID());
                                }
                            }
                            view.addButton.setEnabled(true);
                            view.targetMarkerList.setEnabled(true);
                        }
                    }
                });
    }


    public FeatureDTO getFeatureDTOForName(String name) {
        for (FeatureDTO featureDTO : featureDTOs) {
            if (featureDTO.getName().equals(name)) {
                return featureDTO;
            }
        }
        return null;
    }

    public void addRelationship() {
        final FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = getFeatureMarkerRelationshipFromGui();
        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(featureMarkerRelationshipDTO, featureMarkerRelationshipDTOs);
        } catch (ValidationException e) {
            setError(e.getMessage());
            return;
        }
        view.disableEntryFields();
        lastSelectedFeatureZdbId = featureMarkerRelationshipDTO.getFeatureDTO().getZdbID();
        FeatureRPCService.App.getInstance().addFeatureMarkerRelationShip(featureMarkerRelationshipDTO, publicationID,
                new FeatureEditCallBack<List<FeatureMarkerRelationshipDTO>>("Failed to create FeatureMarkerRelation: " +
                        featureMarkerRelationshipDTO, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.enableEntryFields();
                    }

                    @Override
                    public void onSuccess(List<FeatureMarkerRelationshipDTO> resultList) {
                        featureMarkerRelationshipDTOs = resultList;
                        populateDataTable();
                        view.revertGUI();
                        view.enableEntryFields();
                        AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.CREATE_FEATURE_RELATIONSHIP, featureMarkerRelationshipDTO.toString()));
                    }
                });
    }

    protected void revertGUI() {
        view.revertGUI();
        loadValues();
    }


    private FeatureMarkerRelationshipDTO getFeatureMarkerRelationshipFromGui() {
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = new FeatureMarkerRelationshipDTO();
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setZdbID(view.featureList.getSelected());
        featureDTO.setName(view.featureList.getSelectedText());
        featureDTO.setFeatureType(FeatureTypeEnum.getTypeForDisplay(view.featureType.getText()));
        featureMarkerRelationshipDTO.setFeatureDTO(featureDTO);
        featureMarkerRelationshipDTO.setPublicationZdbID(publicationID);

        featureMarkerRelationshipDTO.setRelationshipType(view.relationshipList.getSelectedText());

        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setZdbID(view.targetMarkerList.getSelected());
        markerDTO.setName(view.targetMarkerList.getSelectedText());

        featureMarkerRelationshipDTO.setMarkerDTO(markerDTO);

        return featureMarkerRelationshipDTO;
    }

    public void onFeatureAddEvent() {
        loadValues(true);
    }

    public void onFeatureNameFilterChange(String featureName) {
        if (featureName.equals(ALL))
            this.featureNameFilter = null;
        else
            this.featureNameFilter = featureName;
        populateDataTable();
    }

    public void onFeatureTypeFilterChange(String featureType) {
        if (featureType.equals(ALL))
            this.featureTypeFilter = null;
        else
            this.featureTypeFilter = featureType;
        populateDataTable();
    }

    private class DeleteFeatureMarkerRelationshipButton extends Button {
        private FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO;

        public DeleteFeatureMarkerRelationshipButton(final FeatureMarkerRelationshipDTO relationshipDTO, final HandlesError handlesError) {
            super("X");
            this.featureMarkerRelationshipDTO = relationshipDTO;

            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    setEnabled(false);
                    FeatureRPCService.App.getInstance().deleteFeatureMarkerRelationship(featureMarkerRelationshipDTO,
                            new FeatureEditCallBack<Void>("Unable to remove feature marker relationship: " + featureMarkerRelationshipDTO, handlesError) {

                                @Override
                                public void onFailure(Throwable throwable) {
                                    super.onFailure(throwable);
                                    setEnabled(true);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    loadValues();
                                    AppUtils.EVENT_BUS.fireEvent(new CurationEvent(EventType.DELETE_FEATURE_RELATIONSHIP, relationshipDTO.toString()));
                                }
                            });
                }
            });
        }

    }

}
