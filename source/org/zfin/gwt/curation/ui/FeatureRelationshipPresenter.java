package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.FeatureMarkerRelationshipDTO;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.FeatureEditCallBack;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ValidationException;

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
        for (FeatureMarkerRelationshipDTO relationshipDTO : featureMarkerRelationshipDTOs) {
            if (featureNameFilter != null && !relationshipDTO.getFeatureDTO().getAbbreviation().equals(featureNameFilter))
                continue;
            if (featureTypeFilter != null && !relationshipDTO.getFeatureDTO().getFeatureType().getDisplay().equals(featureTypeFilter))
                continue;
            view.addFeatureCell(relationshipDTO.getFeatureDTO(), elementIndex);
            view.addFeatureTypeCell(relationshipDTO.getFeatureDTO(), elementIndex);
            view.addFeatureRelationshipCell(relationshipDTO.getRelationshipType(), elementIndex);
            view.addTargetMarker(relationshipDTO.getMarkerDTO(), elementIndex);
            view.addDeletButton(new DeleteFeatureMarkerRelationshipButton(relationshipDTO, this), elementIndex);
            elementIndex++;
        }
        view.endTableUpdate();
    }

    private void loadValues() {
        // retrieve Filter elements
        loadFilterValues();

        // get Feature-Marker-Relationships
        FeatureRPCService.App.getInstance().getFeaturesMarkerRelationshipsForPub(publicationID,
                new FeatureEditCallBack<List<FeatureMarkerRelationshipDTO>>(
                        "Failed to find feature marker relationships for this pub: "
                                + publicationID, this) {
                    @Override
                    public void onSuccess(List<FeatureMarkerRelationshipDTO> featureMarkerRelationshipDTOList) {
                        featureMarkerRelationshipDTOs = featureMarkerRelationshipDTOList;
                        populateDataTable();
                    }
                });

        // collect features for pub
        loadFeatureList();

    }

    private void loadFeatureList() {
        FeatureRPCService.App.getInstance().getFeaturesForPub(publicationID,
                new FeatureEditCallBack<List<FeatureDTO>>("Problem finding features for pub: " + publicationID + " ", this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.featureToAddList.setEnabled(false);
                    }

                    @Override
                    public void onSuccess(List<FeatureDTO> features) {
                        featureDTOs = features;
                        if (featureDTOs != null) {
                            view.featureToAddList.clear();
                            view.featureToAddList.addItem("-----------");
                            for (FeatureDTO featureDTO : featureDTOs) {
                                view.featureToAddList.addItem(featureDTO.getName(), featureDTO.getZdbID());
                            }
                            view.featureToAddList.setEnabled(true);
                        }

                        if (lastSelectedFeatureZdbId != null) {
                            view.featureToAddList.setIndexForValue(lastSelectedFeatureZdbId);
                            lastSelectedFeatureZdbId = null;
///                            featureAddListChanged();
                        }
                    }
                });
    }

    private void loadFilterValues() {
        FeatureRPCService.App.getInstance().getFeaturesForPub(publicationID,
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
                            ///refilter();
                            ///clearError();
                        }
                    }
                });
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

    public void onFeatureSelectionChange(String selectedText) {
        final FeatureDTO featureDTO = getFeatureDTOForName(selectedText);
        if (featureDTO != null && featureDTO.getFeatureType() != null) {
            view.featureToAddType.setText(featureDTO.getFeatureType().getDisplay());
        } else {
            setError("Feature type was null");
            return;
        }
        FeatureRPCService.App.getInstance().getRelationshipTypesForFeatureType(featureDTO.getFeatureType(),
                new FeatureEditCallBack<List<String>>("Failed to return feature relationships for type: " + featureDTO.getFeatureType().getDisplay(), this) {
                    @Override
                    public void onSuccess(List<String> result) {
                        view.featureToAddRelationship.clear();
                        if (result != null && result.size() > 0) {
                            if (result.size() == 1) {
                                // this is probably correct so we don't need to screen it
                                view.featureToAddRelationship.addItem(result.get(0));
                            } else {
                                view.featureToAddRelationship.addItem("-------");
                                for (String rel : result) {
                                    // see case 6337
                                    // is_allele relationship should only be available for transgenic insertions where the known insertion site box is checked
                                    // unspecified transgenic will never have known insertion sites
                                    if ((featureDTO.getFeatureType() == FeatureTypeEnum.TRANSGENIC_INSERTION
                                    )
                                            && rel.startsWith("is allele of")) {
                                        if (featureDTO.getKnownInsertionSite()) {
                                            view.featureToAddRelationship.addItem(rel);
                                        }
                                    } else {
                                        view.featureToAddRelationship.addItem(rel);
                                    }
                                }
                            }
                            view.featureToAddRelationship.setEnabled(true);
                        }
///                        updateTargets();
                    }
                }
        );

    }

    protected void updateTargetGeneList(String selectedFeature, final String selectedRelationship) {
        view.addButton.setEnabled(false);
        view.featureToAddTarget.setEnabled(false);
        final String mutagenForFeature = getFeatureDTOForName(selectedFeature).getMutagen();
        FeatureRPCService.App.getInstance().getMarkersForFeatureRelationAndSource(selectedRelationship, publicationID,
                new FeatureEditCallBack<List<MarkerDTO>>("Failed to find markers for type[" + view.featureToAddType.getText() + "] and pub: " +
                        publicationID, this) {
                    @Override
                    public void onSuccess(List<MarkerDTO> markers) {
                        view.featureToAddTarget.clear();
                        Collections.sort(markers);
                        if (markers != null & markers.size() > 0) {
                            for (MarkerDTO m : markers) {
                                if (mutagenForFeature != null) {
                                    if (selectedRelationship.equalsIgnoreCase("created by")) {
                                        if (m.getName().startsWith(mutagenForFeature)) {
                                            view.featureToAddTarget.addItem(m.getName(), m.getZdbID());
                                        } else if (mutagenForFeature.equals("DNA and CRISPR") && m.getName().startsWith("CRISPR")) {
                                            view.featureToAddTarget.addItem(m.getName(), m.getZdbID());
                                        } else if (mutagenForFeature.equals("DNA and TALEN") && m.getName().startsWith("TALEN")) {
                                            view.featureToAddTarget.addItem(m.getName(), m.getZdbID());
                                        }
                                    } else {
                                        view.featureToAddTarget.addItem(m.getName(), m.getZdbID());
                                    }
                                } else {
                                    view.featureToAddTarget.addItem(m.getName(), m.getZdbID());
                                }
                            }
                            view.addButton.setEnabled(true);
                            view.featureToAddTarget.setEnabled(true);
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
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = getFeatureMarkerRelationshipFromGui();
        try {
            FeatureMarkerRelationshipValidationService.validateFeatureMarkerRelationshipToAdd(featureMarkerRelationshipDTO, featureMarkerRelationshipDTOs);
        } catch (ValidationException e) {
            setError(e.getMessage());
            return;
        }
        view.disableEntryFields();
        lastSelectedFeatureZdbId = featureMarkerRelationshipDTO.getFeatureDTO().getZdbID();
        FeatureRPCService.App.getInstance().addFeatureMarkerRelationShip(featureMarkerRelationshipDTO,
                new FeatureEditCallBack<Void>("Failed to create FeatureMarkerRelation: " +
                        featureMarkerRelationshipDTO, this) {

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        view.enableEntryFields();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        revertGUI();
                        clearError();
                    }
                });
    }

    protected void revertGUI() {
        view.revertGUI();
        loadValues();
//        featureFilterModule.refilter();
    }


    private FeatureMarkerRelationshipDTO getFeatureMarkerRelationshipFromGui() {
        FeatureMarkerRelationshipDTO featureMarkerRelationshipDTO = new FeatureMarkerRelationshipDTO();
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setZdbID(view.featureToAddList.getSelected());
        featureDTO.setName(view.featureToAddList.getSelectedText());
        featureDTO.setFeatureType(FeatureTypeEnum.getTypeForDisplay(view.featureToAddType.getText()));
        featureMarkerRelationshipDTO.setFeatureDTO(featureDTO);
        featureMarkerRelationshipDTO.setPublicationZdbID(publicationID);

        featureMarkerRelationshipDTO.setRelationshipType(view.featureToAddRelationship.getSelectedText());

        MarkerDTO markerDTO = new MarkerDTO();
        markerDTO.setZdbID(view.featureToAddTarget.getSelected());
        markerDTO.setName(view.featureToAddTarget.getSelectedText());

        featureMarkerRelationshipDTO.setMarkerDTO(markerDTO);

        return featureMarkerRelationshipDTO;
    }

    public void onFeatureAddEvent() {
        loadFilterValues();
        loadValues();
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

        public DeleteFeatureMarkerRelationshipButton(FeatureMarkerRelationshipDTO relationshipDTO, final HandlesError handlesError) {
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
                                }
                            });
                }
            });
        }

    }

}
