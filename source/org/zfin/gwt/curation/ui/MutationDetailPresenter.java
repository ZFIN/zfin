package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.MutationDetailControlledVocabularyTermDTO;
import org.zfin.gwt.root.dto.MutationDetailProteinChangeDTO;
import org.zfin.gwt.root.dto.MutationDetailTranscriptChangeDTO;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutationDetailPresenter {

    public static final String MISSENSE = "SO:0001583";
    public static final String STOP_GAIN = "SO:0001587";
    protected AbstractFeatureView view;
    protected Set<MutationDetailTranscriptChangeDTO> dtoSet = new HashSet<>(3);
    protected FeatureDTO dto;

    public MutationDetailPresenter(AbstractFeatureView view) {
        this.view = view;
    }

    public void go() {
        // retrieve DNA change list
        FeatureRPCService.App.getInstance().getDnaChangeList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setNucleotideChangeList(termList, view);
            }

            private void setNucleotideChangeList(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailDnaView.nucleotideChangeList.clear();
                featureView.mutationDetailDnaView.nucleotideChangeList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.mutationDetailDnaView.nucleotideChangeList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve DNA localization
        FeatureRPCService.App.getInstance().getDnaLocalizationChangeList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setGeneLocalizationList(termList, view);
            }

            private void setGeneLocalizationList(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailDnaView.localizationTerm.clear();
                featureView.mutationDetailDnaView.localizationTerm.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.mutationDetailDnaView.localizationTerm.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve Protein consequence list
        FeatureRPCService.App.getInstance().getProteinConsequenceList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setProteinConsequenceList(termList, view);
            }

            private void setProteinConsequenceList(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailProteinView.proteinTermList.clear();
                featureView.mutationDetailProteinView.proteinTermList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList)
                    featureView.mutationDetailProteinView.proteinTermList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
            }
        });
        // retrieve amino acid list
        FeatureRPCService.App.getInstance().getAminoAcidList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setProteinLists(termList, view);
            }

            private void setProteinLists(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailProteinView.proteinWTTermList.clear();
                featureView.mutationDetailProteinView.proteinWTTermList.addItem("----");
                featureView.mutationDetailProteinView.proteinMutatedTerm.clear();
                featureView.mutationDetailProteinView.proteinMutatedTerm.addItem("----");
                int index = 0;
                for (MutationDetailControlledVocabularyTermDTO dto : termList) {
                    // do not include stop item in Wildtype list
                    String displayName = dto.getDisplayName();
                    if (!dto.getTerm().getOboID().equals("SO:0000319"))
                        displayName += " [" + dto.getAbbreviation() + "]";
                    if (index != 0) {
                        featureView.mutationDetailProteinView.proteinWTTermList.addItem(displayName, dto.getTerm().getOboID());
                    }
                    featureView.mutationDetailProteinView.proteinMutatedTerm.addItem(displayName, dto.getTerm().getOboID());
                    index++;
                }
            }
        });
        // retrieve transcript consequence list
        FeatureRPCService.App.getInstance().getTranscriptConsequenceList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setTranscriptConsequenceList(termList, view);
            }

            private void setTranscriptConsequenceList(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailTranscriptView.consequenceList.clear();
                featureView.mutationDetailTranscriptView.consequenceList.addItem("----");
                for (MutationDetailControlledVocabularyTermDTO dto : termList) {
                    featureView.mutationDetailTranscriptView.consequenceList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
                }
            }
        });
    }

    private void populateTranscriptDataTable() {
        int elementIndex = 0;

        if (dtoSet.isEmpty()) {
            view.mutationDetailTranscriptView.emptyDataTable();
            return;
        }
        for (MutationDetailTranscriptChangeDTO dto : dtoSet) {
            DeleteTranscriptConsequence deleteAnchor = new DeleteTranscriptConsequence(dto, view);
            view.mutationDetailTranscriptView.addConsequenceRow(dto, deleteAnchor, elementIndex);
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
        if (getDtoSet().isEmpty()) {
            MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
            dto.setConsequenceOboID(MISSENSE);
            dto.setConsequenceName("missense");
            dtoSet.add(dto);
            populateTranscriptDataTable();
        }
    }

    public void setStopGainTerm() {
        if (getDtoSet().isEmpty()) {
            MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
            dto.setConsequenceOboID(STOP_GAIN);
            dto.setConsequenceName("premature stop");
            dtoSet.add(dto);
            populateTranscriptDataTable();
        }
    }

    public void setDto(FeatureDTO dto) {
        this.dto = dto;
    }

    public boolean isTranscriptDtoSetEmpty() {
        return dtoSet == null || dtoSet.isEmpty();
    }

    public void handleDirty() {

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

        AbstractFeatureView view;

        public DeleteTranscriptConsequence(final MutationDetailTranscriptChangeDTO dto, final AbstractFeatureView view) {
            super("(X)");

            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dtoSet.remove(dto);
                    populateTranscriptDataTable();
                    view.mutationDetailTranscriptView.resetGUI();
                    handleDirty();
                }
            });
        }

    }

}
