package org.zfin.gwt.root.ui;

import com.google.gwt.event.dom.client.BlurHandler;

import java.util.ArrayList;
import java.util.List;

public class ZfinAccessionBoxPresenter {

    private ZfinAccessionBox view;
    private List<BlurHandler> eventHandlerList = new ArrayList<>();

    public ZfinAccessionBoxPresenter(ZfinAccessionBox view) {
        this.view = view;
    }

    public void checkValidAccession(String accessionNumber, final String type) {
        AccessionRPCService.App.getInstance().isValidAccession(accessionNumber, type, new ZfinAsyncCallback<String>("Failed to read valid accession", null) {
                    @Override
                    public void onSuccess(String dbName) {
                        boolean valid = dbName != null;
                        if (type.equals("DNA")) {
                            view.validSequenceCharacter.setVisible(valid);
                            view.faultySequenceCharacter.setVisible(!valid);
                            if (valid)
                                view.validSequenceCharacter.setTitle(dbName);
                        } else {
                            view.validSequenceCharacter.setVisible(valid);
                            view.faultySequenceCharacter.setVisible(!valid);
                            if (valid)
                                view.validSequenceCharacter.setTitle(dbName);
                        }
                        if (!valid) {
                            view.setError("Not an NCBI accession number.");
                        }
                    }
                }
        );

    }

    public void addBlurHandler(BlurHandler eventHandler) {
        eventHandlerList.add(eventHandler);
    }

    public void runPostOnSequenceBlurEvent() {
        for (BlurHandler handler : eventHandlerList) {
            handler.onBlur(null);
        }
    }
}
