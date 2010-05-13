package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.event.RelatedEntityAdapter;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.AbstractInferenceListBox;
import org.zfin.gwt.root.ui.StackComposite;

import java.util.Set;
import java.util.TreeSet;

/**
 */
public class GoCurationInferenceListBox extends AbstractInferenceListBox {


    public GoCurationInferenceListBox() {
        stackTable.setStyleName(DIRTY_STYLE);
    }


    @Override
    protected void addToGUI(String name) {
        GoEvidenceDTO goEvidenceDTO = new GoEvidenceDTO();
        goEvidenceDTO.setName(name);
        goEvidenceDTO.setZdbID(dto.getZdbID());
        goEvidenceDTO.setDataZdbID(dto.getDataZdbID());
        StackComposite<GoEvidenceDTO> stackComposite = new InferenceEntry<GoEvidenceDTO>(goEvidenceDTO, name);

        stackComposite.addRelatedEntityListener(new RelatedEntityAdapter<GoEvidenceDTO>() {
            @Override
            public void removeRelatedEntity(final RelatedEntityEvent<GoEvidenceDTO> event) {
                removeFromGUI(event.getDTO().getName());
                notWorking();
            }
        });


        int rowCount = stackTable.getRowCount();
        stackTable.setWidget(rowCount, 0, stackComposite);
        resetInput();
        notWorking();
        fireEventSuccess();
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    public boolean setStackToDirty(boolean dirty) {
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            ((StackComposite) stackTable.getWidget(i, 0)).setDirty(dirty);
        }
        availableList.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        lookupBox.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        addButton.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        inferenceCategoryList.setStyleName((dirty ? DIRTY_STYLE : CLEAN_STYLE));
        return dirty;
    }

    @Override
    protected int findRowForName(String name) {
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String widgetName = ((InferenceEntry) stackTable.getWidget(i, 0)).getInference();
            if (widgetName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected GoEvidenceDTO createDTOFromGUI() {
        // since all we handle is the inferreds, we will assume that we have the correct DTO.
        if (dto == null) return null;
        GoEvidenceDTO dtoCopy = dto.deepCopy();
        Set<String> inferences = new TreeSet<String>();
        int rowCount = stackTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            inferences.add(((InferenceEntry) stackTable.getWidget(i, 0)).getInference());
        }
        dtoCopy.setInferredFrom(inferences);
        return dtoCopy;
    }

}