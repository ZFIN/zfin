
package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.curation.ui.FeatureRPCService;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.MutationDetailControlledVocabularyTermDTO;
import org.zfin.gwt.root.dto.MutationDetailProteinChangeDTO;
import org.zfin.gwt.root.dto.MutationDetailTranscriptChangeDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutationDetailPresenter {

    public static final String MISSENSE = "SO:0001583";
    public static final String STOP_GAIN = "SO:0001587";
    protected MutationDetailWidgets view;
    protected Set<MutationDetailTranscriptChangeDTO> dtoSet = new HashSet<>(3);
    protected FeatureDTO dto;

    public MutationDetailPresenter(MutationDetailWidgets view) {
        this.view = view;
    }

    public void go() {
        go(null);
    }

    /**
     * Loads the controlled vocabularies into the DNA, transcript and protein lists.
     *
     * @param afterListsLoaded run once all five lists are filled -- values can only be selected
     *                         after that (a variant block populated from a saved variant), or null
     */
    public void go(final Runnable afterListsLoaded) {
        final int[] pendingLists = {5};
        final Runnable listLoaded = () -> {
            pendingLists[0]--;
            if (pendingLists[0] == 0 && afterListsLoaded != null) {
                afterListsLoaded.run();
            }
        };
        // retrieve DNA change list
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_DNA_CHANGE_LIST_START);
        FeatureRPCService.App.getInstance().getDnaChangeList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setNucleotideChangeList(termList, view);
                AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_DNA_CHANGE_LIST_STOP);
                listLoaded.run();
            }

            private void setNucleotideChangeList(List<MutationDetailControlledVocabularyTermDTO> termList, MutationDetailWidgets featureView) {
                featureView.getMutationDetailDnaView().nucleotideChangeList.clear();
                featureView.getMutationDetailDnaView().nucleotideChangeList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.getMutationDetailDnaView().nucleotideChangeList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve DNA localization
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_DNA_LOCALIZATION_CHANGE_LIST_START);
        FeatureRPCService.App.getInstance().getDnaLocalizationChangeList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setGeneLocalizationList(termList, view);
                AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_DNA_LOCALIZATION_CHANGE_LIST_STOP);
                listLoaded.run();
            }

            private void setGeneLocalizationList(List<MutationDetailControlledVocabularyTermDTO> termList, MutationDetailWidgets featureView) {
                featureView.getMutationDetailDnaView().localizationTerm.clear();
                featureView.getMutationDetailDnaView().localizationTerm.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.getMutationDetailDnaView().localizationTerm.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve Protein consequence list
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_PROTEIN_CONSEQUENCE_LIST_START);
        FeatureRPCService.App.getInstance().getProteinConsequenceList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setProteinConsequenceList(termList, view);
                AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_PROTEIN_CONSEQUENCE_LIST_STOP);
                listLoaded.run();
            }

            private void setProteinConsequenceList(List<MutationDetailControlledVocabularyTermDTO> termList, MutationDetailWidgets featureView) {
                featureView.getMutationDetailProteinView().proteinTermList.clear();
                featureView.getMutationDetailProteinView().proteinTermList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.getMutationDetailProteinView().proteinTermList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve amino acid list
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_AMINO_ACID_LIST_START);
        FeatureRPCService.App.getInstance().getAminoAcidList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setProteinLists(termList, view);
                AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_AMINO_ACID_LIST_STOP);
                listLoaded.run();
            }

            private void setProteinLists(List<MutationDetailControlledVocabularyTermDTO> termList, MutationDetailWidgets featureView) {
                featureView.getMutationDetailProteinView().proteinWTTermList.clear();
                featureView.getMutationDetailProteinView().proteinWTTermList.addItem("----");
                featureView.getMutationDetailProteinView().proteinMutatedTerm.clear();
                featureView.getMutationDetailProteinView().proteinMutatedTerm.addItem("----");
                int index = 0;
                for (MutationDetailControlledVocabularyTermDTO dto : termList) {
                    // do not include stop item in Wildtype list
                    String displayName = dto.getDisplayName();
                    if (!dto.getTerm().getOboID().equals("SO:0000319"))
                        displayName += " [" + dto.getAbbreviation() + "]";



                    featureView.getMutationDetailProteinView().proteinMutatedTerm.addItem(displayName, dto.getTerm().getOboID());
                    featureView.getMutationDetailProteinView().proteinWTTermList.addItem(displayName, dto.getTerm().getOboID());
                    index++;
                }
            }
        });
        // retrieve transcript consequence list
        AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_TRANSCRIPT_CONSEQUENCE_LIST_START);
        FeatureRPCService.App.getInstance().getTranscriptConsequenceList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setTranscriptConsequenceList(termList, view);
                AppUtils.fireAjaxCall(FeatureModule.getModuleInfo(), AjaxCallEventType.GET_TRANSCRIPT_CONSEQUENCE_LIST_STOP);
                listLoaded.run();
            }

            private void setTranscriptConsequenceList(List<MutationDetailControlledVocabularyTermDTO> termList, MutationDetailWidgets featureView) {
                featureView.getMutationDetailTranscriptView().consequenceList.clear();
                featureView.getMutationDetailTranscriptView().consequenceList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList) {
                    featureView.getMutationDetailTranscriptView().consequenceList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
                }
            }
        });
    }

    private void populateTranscriptDataTable() {
        int elementIndex = 0;

        if (dtoSet.isEmpty()) {
            view.getMutationDetailTranscriptView().emptyDataTable();
            return;
        }
        for (MutationDetailTranscriptChangeDTO dto : dtoSet) {
            DeleteTranscriptConsequence deleteAnchor = new DeleteTranscriptConsequence(dto, view);
            view.getMutationDetailTranscriptView().addConsequenceRow(dto, deleteAnchor, elementIndex);
            elementIndex++;
        }
    }

    public void rebuildGUI() {
        if (dtoSet != null)
            dtoSet.clear();
    }

    public void addTranscriptConsequence(MutationDetailTranscriptChangeDTO dto) {
        dtoSet.add(dto);
        populateTranscriptDataTable();
    }

    public Set<MutationDetailTranscriptChangeDTO> getDtoSet() {
        return dtoSet;
    }


    public void setDtoSet(Set<MutationDetailTranscriptChangeDTO> dtoSetTranscript) {
        if (dtoSetTranscript == null) {
            dtoSet = new HashSet<>(5);
        } else {
            // ensure that we work on a copy and not the original collection
            // works as long as there are no objects on the individual entity: it's only a shallow copy
            // copy constructor
            dtoSet = new HashSet<>(dtoSetTranscript);
        }
        populateTranscriptDataTable();
    }

    protected void setMissenseTerm() {
        setTranscriptConsequenceTerm(getMissenseDTO(), getStopGainDTO());
    }

    public void setStopGainTerm() {
        setTranscriptConsequenceTerm(getStopGainDTO(), getMissenseDTO());
    }

    public void setTranscriptConsequenceTerm(MutationDetailTranscriptChangeDTO newDto, MutationDetailTranscriptChangeDTO oldDto) {
        // if transcript consequences exist make sure oldDto is removed
        if (!getDtoSet().isEmpty()) {
            for (MutationDetailTranscriptChangeDTO dto : getDtoSet()) {
                if (dto.getConsequenceOboID().equals(oldDto.getConsequenceOboID())) {
                    getDtoSet().remove(dto);
                }
            }
            // if newDto consequence already exists we are done
            for (MutationDetailTranscriptChangeDTO dto : getDtoSet()) {
                if (dto.getConsequenceOboID().equals(newDto.getConsequenceOboID())) {
                    return;
                }
            }
        }
        dtoSet.add(newDto);
        populateTranscriptDataTable();
    }

    private MutationDetailTranscriptChangeDTO getStopGainDTO() {
        return getTranscriptDTO(STOP_GAIN, "premature stop");
    }

    private MutationDetailTranscriptChangeDTO getMissenseDTO() {
        return getTranscriptDTO(MISSENSE, "missense");
    }

    private MutationDetailTranscriptChangeDTO getTranscriptDTO(String oboID, String consequenceName) {
        MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
        dto.setConsequenceOboID(oboID);
        dto.setConsequenceName(consequenceName);
        return dto;
    }

    public void setDto(FeatureDTO dto) {
        this.dto = dto;
    }

    public boolean isTranscriptDtoSetEmpty() {
        return dtoSet == null || dtoSet.isEmpty();
    }

    public void handleDirty(){

    }

    public String isValid(FeatureDTO featureDTO) {
        MutationDetailProteinChangeDTO proteinChanges = featureDTO.getProteinChangeDTO();
        if (proteinChanges != null) {
            if (proteinChanges.getMutantAATermOboID() != null &&
                    proteinChanges.getMutantAATermOboID().equals(proteinChanges.getWildtypeAATermOboID())
                    )
                return "Cannot have both amino acids the same!";
            if (proteinChanges.getMutantAATermOboID() != null && proteinChanges.getWildtypeAATermOboID() == null ||
                    proteinChanges.getMutantAATermOboID() == null && proteinChanges.getWildtypeAATermOboID() != null)
                return "Please select both the amino acids for a change or none.";

        }
        return null;
    }

    ////// Handlers and Listeners....

    private class DeleteTranscriptConsequence extends Anchor {

        public DeleteTranscriptConsequence(final MutationDetailTranscriptChangeDTO dto, final MutationDetailWidgets view) {
            super("(X)");

            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dtoSet.remove(dto);
                    populateTranscriptDataTable();
                    view.getMutationDetailTranscriptView().resetGUI();
                    handleDirty();
                }
            });
        }

    }

}
