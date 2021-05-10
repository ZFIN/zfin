package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.AjaxCallEventType;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.ui.ZfinModule;

import java.util.List;

public class RetrieveRelatedEntityDTOListCallBack<T extends RelatedEntityDTO> extends ZfinAsyncCallback<List<T>> {

    private ListBox entityList;
    private List<T> dtoList;
    private boolean leaveFirstEntryBlank;

    public RetrieveRelatedEntityDTOListCallBack(ListBox listBox, String errorMessage, ErrorHandler errorLabel,
                                                ZfinModule module, AjaxCallEventType eventType) {
        super(errorMessage, errorLabel, module, eventType);
        this.entityList = listBox;
    }

    @Override
    public void onSuccess(List<T> dtoList) {
        super.onFinish();
        entityList.clear();
        if (leaveFirstEntryBlank) {
            entityList.addItem("");
        }
        if (dtoList == null || dtoList.size() == 0)
            return;
        this.dtoList = dtoList;

        for (T dto : dtoList) {
            entityList.addItem(dto.getName(), dto.getZdbID());
        }
    }

    public List<T> getDtoList() {
        return dtoList;
    }

    public void setLeaveFirstEntryBlank(boolean leaveFirstEntryBlank) {
        this.leaveFirstEntryBlank = leaveFirstEntryBlank;
    }

}
