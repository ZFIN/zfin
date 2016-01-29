package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.TermInfoCallBack;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.LookupRPCService;
import org.zfin.gwt.root.util.LookupRPCServiceAsync;
import org.zfin.gwt.root.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * construction zone
 */
public class FxCurationPresenter implements Presenter {

    private PileStructuresRPCAsync pileStructureRPCAsync = PileStructuresRPC.App.getInstance();
    private LookupRPCServiceAsync lookupRPC = LookupRPCService.App.getInstance();

    private ConstructionZoneModule view;
    private String publicationID;
    private List<EapQualityTermDTO> fullQualityList = new ArrayList<>();
    private Map<CheckBox, EapQualityTermDTO> checkBoxMap = new HashMap<>();

    public FxCurationPresenter(ConstructionZoneModule view, String publicationID) {
        this.view = view;
        this.publicationID = publicationID;
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

    public void submitStructure() {
        // expect only 1-2 checked normally
        List<EapQualityTermDTO> eapQualityList = new ArrayList<>(4);
        for (CheckBox checkBox : view.getQualityCheckBoxList()) {
            if (checkBox.getValue()) {
                eapQualityList.add(checkBoxMap.get(checkBox));
            }
        }
        //Window.alert("num of Qualities " + eapQualityList.get(0).getNickName());
        // can submit the 'not-expressed' only without any eap selected.
        if (view.getNotExpressedCheckBox().getValue()) {
            if (eapQualityList.size() > 0) {
                setError("Cannot submit a 'not expressed' with one or more qualities");
                return;
            }
        }
        getExpressedTerm(eapQualityList);
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
        List<ExpressedTermDTO> expressedTermDTOList;
        if (eapQualityList == null || eapQualityList.size() == 0) {
            expressedTermDTOList = new ArrayList<>(1);
            termDTO.setExpressionFound(!view.getNotExpressedCheckBox().getValue());
            expressedTermDTOList.add(termDTO);
        } else {
            expressedTermDTOList = new ArrayList<>(eapQualityList.size());
            for (EapQualityTermDTO eap : eapQualityList) {
                ExpressedTermDTO dto = termDTO.clone();
                dto.setQualityTerm(eap);
                dto.setExpressionFound(true);
                if (eap.getNickName().equals(EapQualityTermDTO.ABSENT_PHENOTYPIC))
                    dto.setExpressionFound(false);
                expressedTermDTOList.add(dto);
            }
        }

        StructureValidator<ExpressedTermDTO> structureValidator = new FxPileStructureValidator(view.getTermEntryMap());
        if (structureValidator.isValidNewPileStructure(termDTO)) {
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

    /**
     * This method takes an ExpressedTermDTO and pre-populates the construction
     * zone with the given entities. The EntityPart defines which part
     * should be displayed in the term info box.
     * A pile structure consists (currently) of Superterm : Subterm : Quality
     *
     * @param term           full post-composed structure
     * @param selectedEntity entity
     */
    public void prepopulateConstructionZone(ExpressedTermDTO term, EntityPart selectedEntity) {
        switch (selectedEntity) {
            case ENTITY_SUPERTERM:
                lookupRPC.getTermInfo(term.getEntity().getSuperTerm().getOntology(), term.getEntity().getSuperTerm().getZdbID(),
                        new TermInfoCallBack(view.termInfoBox, term.getEntity().getSuperTerm().getZdbID()));
                break;
            case ENTITY_SUBTERM:
                lookupRPC.getTermInfo(term.getEntity().getSubTerm().getOntology(), term.getEntity().getSubTerm().getZdbID(),
                        new TermInfoCallBack(view.termInfoBox, term.getEntity().getSubTerm().getZdbID()));
                break;
        }
        view.populateFullTerm(term);

    }


    class RetrieveEapQualityListCallBack extends ZfinAsyncCallback<List<EapQualityTermDTO>> {

        public RetrieveEapQualityListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<EapQualityTermDTO> qualityTermDTOList) {
            fullQualityList.clear();
            fullQualityList = qualityTermDTOList;
            view.initializeEapQualityList(qualityTermDTOList);
            int index = 0;
            for (CheckBox box : view.getQualityCheckBoxList())
                checkBoxMap.put(box, qualityTermDTOList.get(index++));
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
        }
    }

    class CreatePileStructureCallback implements AsyncCallback<List<ExpressionPileStructureDTO>> {

        public void onFailure(Throwable throwable) {
            setError(throwable.getMessage());
        }

        /**
         * Returns the pile Structure entity
         *
         * @param pileStructureList pile Structure
         */
        public void onSuccess(List<ExpressionPileStructureDTO> pileStructureList) {
            ExpressionEvent event = new ExpressionEvent();
            event.setStructureDTOList(pileStructureList);
            AppUtils.EVENT_BUS.fireEvent(event);
            view.resetButton.click();
            clearErrorMessages();
        }
    }
}
