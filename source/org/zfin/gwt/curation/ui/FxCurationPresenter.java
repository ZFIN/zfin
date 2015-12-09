package org.zfin.gwt.curation.ui;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * construction zone
 */
public class FxCurationPresenter implements Presenter {

    private final HandlerManager eventBus;
    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();
    private ConstructionZoneModule view;
    private String publicationID;
    private List<EapQualityTermDTO> fullQualityList = new ArrayList<>();
    private Map<CheckBox, EapQualityTermDTO> checkBoxMap = new HashMap<>();
    private boolean processing = false;

    public FxCurationPresenter(HandlerManager eventBus, ConstructionZoneModule view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
    }

    public void bind() {
/*
        view.getEnvironmentSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
*/
/*
        view.getAddDiseaseModelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (processing)
                    return;
                processing = true;
                DiseaseAnnotationDTO disease = getDiseaseModel();

                if (disease == null) {
                    processing = false;
                    return;
                }


                diseaseRpcService.addHumanDiseaseAnnotation(disease, new RetrieveEapQualityListCallBack("Could not add a new disease model", view.getErrorLabel()));


                view.getLoadingImage().setVisible(true);
            }
        });
*/
        addDynamicClickHandler();
    }

    List<CheckBox> checkBoxList;

    private void addDynamicClickHandler() {
        checkBoxList = view.getQualityCheckBoxList();

        for (CheckBox checkBox : checkBoxList) {
        }

    }


    @Override
    public void go() {
        createQualityList();
    }

    private void createQualityList() {
        // list of Eap qualities
        pileStructureRPCAsync.getEapQualityListy(new RetrieveEapQualityListCallBack(null, view.getErrorElement()));
    }

    public void setError(String message) {
        view.getErrorElement().setText(message);
    }

    public void clearErrorMessages() {
        view.getErrorElement().setError("");
    }

    private void resetUI() {
        view.getErrorElement().clearAllErrors();
        clearErrorMessages();
    }

    public void clearQualityChecks() {
        view.updateEapQualityList(fullQualityList);
    }

    public void submitStructure() {
        // expect only 1-2 checked normally
        List<EapQualityTermDTO> eapQualityList = new ArrayList<>(4);
        for (CheckBox checkBox : view.getQualityCheckBoxList()) {
            if (checkBox.getValue()) {
                eapQualityList.add(checkBoxMap.get(checkBox));
            }
        }
        if (eapQualityList != null && eapQualityList.size() > 0) {
            ExpressedTermDTO expressedTerm = getExpressedTerm(eapQualityList);
            //pileStructureRPCAsync.createPileStructure();
        }
    }

    private ExpressedTermDTO getExpressedTerm(List<EapQualityTermDTO> eapQualityList) {
        EntityDTO entityDTO = new EntityDTO();
        ExpressedTermDTO termDTO = new ExpressedTermDTO();
        termDTO.setEntity(entityDTO);
        for (Map.Entry<EntityPart, TermEntry> postComposedPartTermEntryEntry : view.getTermEntryUnitsMap().entrySet()) {
            TermEntry termEntry = postComposedPartTermEntryEntry.getValue();
            switch (postComposedPartTermEntryEntry.getKey()) {
                case ENTITY_SUPERTERM:
                    if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                        entityDTO.setSuperTerm(getTermDTO(termEntry));
                    }
                    break;
                case ENTITY_SUBTERM:
                    if (StringUtils.isNotEmpty(termEntry.getTermText())) {
                        entityDTO.setSubTerm(getTermDTO(termEntry));
                    }
                    break;
            }
        }
        List<ExpressedTermDTO> expressedTermDTOList = new ArrayList<>(eapQualityList.size());
        if (eapQualityList == null || eapQualityList.size() == 0)
            expressedTermDTOList.add(termDTO);
        else {
            for (EapQualityTermDTO eap : eapQualityList) {
                ExpressedTermDTO dto = termDTO.clone();
                dto.setQualityTerm(eap);
                expressedTermDTOList.add(dto);
            }
        }
        StructureValidator structureValidator = new FxPileStructureValidator(view.getTermEntryMap());
        if (structureValidator.isValidNewPileStructure(termDTO)) {
/*
            if (structurePile.hasStructureOnPile(termDTO)) {
                setError("Structure [" + termDTO.getDisplayName() + "] already on pile.");
                return null;
            }
*/
            pileStructureRPCAsync.createPileStructure(expressedTermDTOList, publicationID, new CreatePileStructureCallback());
            clearErrorMessages();
        } else {
            setError(structureValidator.getErrorMessage());
        }
        return null;
    }

    private TermDTO getTermDTO(TermEntry termEntry) {
        TermDTO superterm = new TermDTO();
        superterm.setName(termEntry.getTermText());
        superterm.setOntology(termEntry.getSelectedOntology());
        return superterm;
    }


    class RetrieveEapQualityListCallBack extends ZfinAsyncCallback<List<EapQualityTermDTO>> {

        public RetrieveEapQualityListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<EapQualityTermDTO> qualityTermDTOList) {
            fullQualityList.clear();
            fullQualityList = qualityTermDTOList;
            view.updateEapQualityList(qualityTermDTOList);
            int index = 0;
            for (CheckBox box : view.getQualityCheckBoxList()) {
                if (index == 0)
                    checkBoxMap.put(box, new EapQualityTermDTO());
                else
                    checkBoxMap.put(box, qualityTermDTOList.get(index));
                index++;
            }
            processing = false;
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            processing = false;
        }
    }

    private class CreatePileStructureCallback implements AsyncCallback<List<ExpressionPileStructureDTO>> {

        public void onFailure(Throwable throwable) {
            setError(throwable.getMessage());
        }

        /**
         * Returns the pile Structure entity
         *
         * @param pileStructureList pile Structure
         */
        public void onSuccess(List<ExpressionPileStructureDTO> pileStructureList) {
            Window.alert("Success");
            // call listeners
/*
            for (PileStructureListener listener : pileListener) {
                listener.onPileStructureCreation(pileStructure);
            }
*/
            view.resetButton.click();
            clearErrorMessages();
        }
    }
}
