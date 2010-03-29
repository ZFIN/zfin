package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

public class VoidAsyncCallback extends ZfinAsyncCallback<Void> {

    VoidAsyncCallback(String errorMessage, ErrorHandler errorHandler, String loadingImageDivName) {
        super(errorMessage, errorHandler, loadingImageDivName);
    }

}

