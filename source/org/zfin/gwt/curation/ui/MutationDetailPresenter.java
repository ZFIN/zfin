package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import org.zfin.gwt.curation.event.DirtyValueEvent;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.IsDirty;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.BooleanCollector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutationDetailPresenter {

    public static final String MISSENSE = "SO:0001583";
    public static final String STOP_GAIN = "SO:0001587";
    private FeatureAddView view;
    private FeatureEditView editView;
    private Set<MutationDetailTranscriptChangeDTO> dtoSet = new HashSet<>(3);
    private FeatureDTO dto;

    public MutationDetailPresenter(FeatureAddView view, FeatureEditView editView) {
        this.view = view;
        this.editView = editView;
    }

    public void go() {
        // retrieve DNA change list
        FeatureRPCService.App.getInstance().getDnaChangeList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setNucleotideChangeList(termList, view);
                setNucleotideChangeList(termList, editView);
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
                setGeneLocalizationList(termList, editView);
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
                setProteinConsequenceList(termList, editView);
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
                setProteinLists(termList, editView);
            }

            private void setProteinLists(List<MutationDetailControlledVocabularyTermDTO> termList, AbstractFeatureView featureView) {
                featureView.mutationDetailProteinView.proteinWTTermList.clear();
                featureView.mutationDetailProteinView.proteinWTTermList.addItem("----");
                featureView.mutationDetailProteinView.proteinMutatedTerm.clear();
                featureView.mutationDetailProteinView.proteinMutatedTerm.addItem("----");
                int index = 0;
                for (MutationDetailControlledVocabularyTermDTO dto : termList) {
                    // do not include stop item in Wildtype list
                    if (index != 0)
                        featureView.mutationDetailProteinView.proteinWTTermList.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
                    featureView.mutationDetailProteinView.proteinMutatedTerm.addItem(dto.getDisplayName(), dto.getTerm().getOboID());
                    index++;
                }
            }
        });
        // retrieve transcript consequence list
        FeatureRPCService.App.getInstance().getTranscriptConsequenceList(new ZfinAsyncCallback<List<MutationDetailControlledVocabularyTermDTO>>("Failed to read Curator info", null) {
            @Override
            public void onSuccess(List<MutationDetailControlledVocabularyTermDTO> termList) {
                setTranscriptConsequenceList(termList, view);
                setTranscriptConsequenceList(termList, editView);
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

    private void populateTranscriptDataTable(AbstractFeatureView view) {
        int elementIndex = 0;
        if (dtoSet == null || dtoSet.isEmpty()) {
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
        dtoSet.clear();
    }

    public void addTranscriptConsequence(MutationDetailTranscriptChangeDTO dto) {
        dtoSet.add(dto);
        populateTranscriptDataTable(view);
    }

    public Set<MutationDetailTranscriptChangeDTO> getDtoSet() {
        return dtoSet;
    }


    public void setDtoSet(Set<MutationDetailTranscriptChangeDTO> dtoSet) {
        this.dtoSet = dtoSet;
        populateTranscriptDataTable(editView);
    }

    public void checkValidAccession(String accessionNumber, final String type) {
        FeatureRPCService.App.getInstance().isValidAccession(accessionNumber, type, new ZfinAsyncCallback<String>("Failed to read valid accession", null) {
                    @Override
                    public void onSuccess(String dbName) {
                        boolean valid = dbName != null;
                        if (type.equals("DNA")) {
                            view.mutationDetailDnaView.validSequenceCharacter.setVisible(valid);
                            view.mutationDetailDnaView.faultySequenceCharacter.setVisible(!valid);
                            if (valid)
                                view.mutationDetailDnaView.validSequenceCharacter.setTitle(dbName);
                        } else {
                            view.mutationDetailProteinView.validSequenceCharacter.setVisible(valid);
                            view.mutationDetailProteinView.faultySequenceCharacter.setVisible(!valid);
                            if (valid)
                                view.mutationDetailProteinView.validSequenceCharacter.setTitle(dbName);
                        }
                    }
                }

        );

    }

    protected void setMissenseTerm(AbstractFeatureView view) {
        if (getDtoSet().isEmpty()) {
            MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
            dto.setConsequenceOboID(MISSENSE);
            dtoSet.add(dto);
            populateTranscriptDataTable(view);
        }
    }

    public void setStopGainTerm(AbstractFeatureView view) {
        if (getDtoSet().isEmpty()) {
            MutationDetailTranscriptChangeDTO dto = new MutationDetailTranscriptChangeDTO();
            dto.setConsequenceOboID(STOP_GAIN);
            dtoSet.add(dto);
            populateTranscriptDataTable(view);
        }
    }

    // for the edit feature section
    public boolean isDirty() {
        MutationDetailDNAView mutationDetailDnaView = editView.mutationDetailDnaView;
        MutationDetailDnaChangeDTO dnaChangeDTO = dto.getDnaChangeDTO();
        BooleanCollector col = new BooleanCollector(false);
        if (dnaChangeDTO != null) {
            col.addBoolean(mutationDetailDnaView.localizationTerm.isDirty(dnaChangeDTO.getLocalizationTermOboID()));
            col.addBoolean(mutationDetailDnaView.nucleotideChangeList.isDirty(dnaChangeDTO.getChangeTermOboId()));
            col.addBoolean(mutationDetailDnaView.plusBasePair.isDirty(dnaChangeDTO.getNumberAddedBasePair()));
            col.addBoolean(mutationDetailDnaView.minusBasePair.isDirty(dnaChangeDTO.getNumberRemovedBasePair()));
            col.addBoolean(mutationDetailDnaView.positionStart.isDirty(dnaChangeDTO.getPositionStart()));
            col.addBoolean(mutationDetailDnaView.positionEnd.isDirty(dnaChangeDTO.getPositionEnd()));
            col.addBoolean(mutationDetailDnaView.exonNumber.isDirty(dnaChangeDTO.getExonNumber()));
            col.addBoolean(mutationDetailDnaView.intronNumber.isDirty(dnaChangeDTO.getIntronNumber()));
            col.addBoolean(mutationDetailDnaView.sequenceOfReference.isDirty(dnaChangeDTO.getSequenceReferenceAccessionNumber()));
        }
        MutationDetailProteinView mutationDetailProteinView = editView.mutationDetailProteinView;
        MutationDetailProteinChangeDTO proteinChangeDTO = dto.getProteinChangeDTO();
        if (proteinChangeDTO == null) {
            for (IsDirty widget : mutationDetailProteinView.getIsDirtyFields())
                col.addBoolean(widget.isDirty(null));

        } else {
            col.addBoolean(mutationDetailProteinView.proteinTermList.isDirty(proteinChangeDTO.getConsequenceTermOboID()));
            col.addBoolean(mutationDetailProteinView.proteinMutatedTerm.isDirty(proteinChangeDTO.getMutantAATermOboID()));
            col.addBoolean(mutationDetailProteinView.proteinWTTermList.isDirty(proteinChangeDTO.getWildtypeAATermOboID()));
            col.addBoolean(mutationDetailProteinView.plusAminoAcid.isDirty(proteinChangeDTO.getNumberAddedAminoAcid()));
            col.addBoolean(mutationDetailProteinView.minusAminoAcid.isDirty(proteinChangeDTO.getNumberRemovedAminoAcid()));
            col.addBoolean(mutationDetailProteinView.positionStart.isDirty(proteinChangeDTO.getPositionStart()));
            col.addBoolean(mutationDetailProteinView.positionEnd.isDirty(proteinChangeDTO.getPositionEnd()));
            col.addBoolean(mutationDetailProteinView.sequenceOfReference.isDirty(proteinChangeDTO.getSequenceReferenceAccessionNumber()));
        }
        return col.arrivedValue();
    }

    public void handleDirty() {
        // notify upper view / presenter logic about dirty or un-dirtied fields.
        DirtyValueEvent event = new DirtyValueEvent();
        event.setDirty(isDirty());
        AppUtils.EVENT_BUS.fireEvent(event);
    }

    public void setDto(FeatureDTO dto) {
        this.dto = dto;
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
                    populateTranscriptDataTable(view);
                    view.mutationDetailTranscriptView.resetGUI();
                }
            });
        }

    }

}
