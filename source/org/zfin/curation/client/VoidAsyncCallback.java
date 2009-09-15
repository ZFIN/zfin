package org.zfin.curation.client;

import org.zfin.curation.client.ZfinAsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class VoidAsyncCallback extends ZfinAsyncCallback<Void> {

    private Widget loadingImage;

    VoidAsyncCallback(Label errorMessage, Widget loadingImage) {
        super("Error", errorMessage);
        this.loadingImage = loadingImage;
    }

    public void onSuccess(Void aVoid) {
    }

    public void onFailureCleanup() {
        loadingImage.setVisible(true);
    }
}

