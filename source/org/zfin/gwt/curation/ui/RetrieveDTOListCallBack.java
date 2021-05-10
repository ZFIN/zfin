package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.ui.ListBox;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.List;

public class RetrieveDTOListCallBack<T extends RelatedEntityDTO> extends ZfinAsyncCallback<List<T>> {

    private ListBox entityList;
    private List<T> dtoList;

    public RetrieveDTOListCallBack(ListBox listBox, String errorMessage, ErrorHandler errorLabel) {
        super(errorMessage, errorLabel);
        this.entityList = listBox;
    }

    @Override
    public void onSuccess(List<T> dtoList) {
        entityList.clear();
        this.dtoList = dtoList;
        int index = 0;
        for (T dto : dtoList) {
            if (dto.getName().startsWith("---")) {
                entityList.addItem(dto.getName(), dto.getZdbID());
                entityList.getElement().getElementsByTagName("option").getItem(index).setAttribute("disabled", "disabled");
            } else
                entityList.addItem(dto.getName(), dto.getZdbID());
            index++;
        }
    }

    public List<T> getDtoList() {
        return dtoList;
    }

}
