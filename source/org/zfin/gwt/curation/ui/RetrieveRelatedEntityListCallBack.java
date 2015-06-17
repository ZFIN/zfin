package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class RetrieveRelatedEntityListCallBack extends ZfinAsyncCallback<List<RelatedEntityDTO>> {

    private ListBox relatedEntityListBox;
    private List<RelatedEntityDTO> dtoList;
    private boolean leaveFirstEntryBlank;

    public RetrieveRelatedEntityListCallBack(ListBox listBox, String errorMessage, ErrorHandler errorLabel) {
        super(errorMessage, errorLabel);
        this.relatedEntityListBox = listBox;
    }

    @Override
    public void onSuccess(List<RelatedEntityDTO> dtoList) {
        relatedEntityListBox.clear();
        if (leaveFirstEntryBlank) {
            relatedEntityListBox.addItem("");
        }
        this.dtoList = dtoList;

        for (RelatedEntityDTO entityDTO : dtoList) {
            relatedEntityListBox.addItem(entityDTO.getName(), entityDTO.getZdbID());
        }
    }

    public List<RelatedEntityDTO> getDtoList() {
        return dtoList;
    }

    public void setLeaveFirstEntryBlank(boolean leaveFirstEntryBlank) {
        this.leaveFirstEntryBlank = leaveFirstEntryBlank;
    }
}
