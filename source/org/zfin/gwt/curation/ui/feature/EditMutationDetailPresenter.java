package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import org.zfin.gwt.curation.event.DirtyValueEvent;
import org.zfin.gwt.curation.ui.feature.FeatureEditView;
import org.zfin.gwt.curation.ui.feature.MutationDetailDNAView;
import org.zfin.gwt.curation.ui.feature.MutationDetailPresenter;
import org.zfin.gwt.curation.ui.feature.MutationDetailProteinView;
import org.zfin.gwt.root.dto.MutationDetailDnaChangeDTO;
import org.zfin.gwt.root.dto.MutationDetailProteinChangeDTO;
import org.zfin.gwt.root.dto.MutationDetailTranscriptChangeDTO;
import org.zfin.gwt.root.ui.IsDirtyWidget;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.BooleanCollector;

import java.util.Set;

public class EditMutationDetailPresenter extends MutationDetailPresenter {

    public EditMutationDetailPresenter(FeatureEditView editView) {
        super(editView);
        view.mutationDetailDnaView.zfinAccessionBox.getPresenter().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                handleDirty();
            }
        });
        view.mutationDetailProteinView.zfinAccessionBox.getPresenter().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent blurEvent) {
                handleDirty();
            }
        });
    }

    // for the edit feature section
    public boolean isDirty() {
        MutationDetailDNAView mutationDetailDnaView = view.mutationDetailDnaView;
        MutationDetailDnaChangeDTO dnaChangeDTO = dto.getDnaChangeDTO();
        BooleanCollector col = new BooleanCollector(true);
        if (dnaChangeDTO == null) {
            for (IsDirtyWidget widget : mutationDetailDnaView.getValueFields())
                col.addBoolean(widget.isDirty(null));

        } else {
            col.addBoolean(mutationDetailDnaView.localizationTerm.isDirty(dnaChangeDTO.getLocalizationTermOboID()));
            col.addBoolean(mutationDetailDnaView.nucleotideChangeList.isDirty(dnaChangeDTO.getChangeTermOboId()));
            col.addBoolean(mutationDetailDnaView.plusBasePair.isDirty(dnaChangeDTO.getNumberAddedBasePair()));
            col.addBoolean(mutationDetailDnaView.minusBasePair.isDirty(dnaChangeDTO.getNumberRemovedBasePair()));
            col.addBoolean(mutationDetailDnaView.positionStart.isDirty(dnaChangeDTO.getPositionStart()));
            col.addBoolean(mutationDetailDnaView.positionEnd.isDirty(dnaChangeDTO.getPositionEnd()));
            col.addBoolean(mutationDetailDnaView.exonNumber.isDirty(dnaChangeDTO.getExonNumber()));
            col.addBoolean(mutationDetailDnaView.intronNumber.isDirty(dnaChangeDTO.getIntronNumber()));
            col.addBoolean(mutationDetailDnaView.zfinAccessionBox.getAccessionNumber().isDirty(dnaChangeDTO.getSequenceReferenceAccessionNumber()));
        }
        MutationDetailProteinView mutationDetailProteinView = view.mutationDetailProteinView;
        MutationDetailProteinChangeDTO proteinChangeDTO = dto.getProteinChangeDTO();
        if (proteinChangeDTO == null) {
            for (IsDirtyWidget widget : mutationDetailProteinView.getValueFields())
                col.addBoolean(widget.isDirty(null));

        } else {
            col.addBoolean(mutationDetailProteinView.proteinTermList.isDirty(proteinChangeDTO.getConsequenceTermOboID()));
            col.addBoolean(mutationDetailProteinView.proteinMutatedTerm.isDirty(proteinChangeDTO.getMutantAATermOboID()));
            col.addBoolean(mutationDetailProteinView.proteinWTTermList.isDirty(proteinChangeDTO.getWildtypeAATermOboID()));
            col.addBoolean(mutationDetailProteinView.plusAminoAcid.isDirty(proteinChangeDTO.getNumberAddedAminoAcid()));
            col.addBoolean(mutationDetailProteinView.minusAminoAcid.isDirty(proteinChangeDTO.getNumberRemovedAminoAcid()));
            col.addBoolean(mutationDetailProteinView.positionStart.isDirty(proteinChangeDTO.getPositionStart()));
            col.addBoolean(mutationDetailProteinView.positionEnd.isDirty(proteinChangeDTO.getPositionEnd()));
            col.addBoolean(mutationDetailProteinView.zfinAccessionBox.getAccessionNumber().isDirty(proteinChangeDTO.getSequenceReferenceAccessionNumber()));
        }
        Set<MutationDetailTranscriptChangeDTO> transcriptChangeDTOSet = dto.getTranscriptChangeDTOSet();
        if (transcriptChangeDTOSet == null || transcriptChangeDTOSet.isEmpty()) {
            // check if there are any transcript records created
            col.addBoolean(!dtoSet.isEmpty());
        } else {
            if (dtoSet != null) {
                for (MutationDetailTranscriptChangeDTO detailDto : dtoSet) {
                    col.addBoolean(!transcriptChangeDTOSet.contains(detailDto));
                }
                for (MutationDetailTranscriptChangeDTO detailDto : transcriptChangeDTOSet) {
                    col.addBoolean(!dtoSet.contains(detailDto));
                }
            }
        }
        return col.arrivedValue();
    }

    public void handleDirty() {
        // notify upper view / presenter logic about dirty or un-dirtied fields.
        DirtyValueEvent event = new DirtyValueEvent();
        event.setDirty(isDirty());
        AppUtils.EVENT_BUS.fireEvent(event);
    }


}
