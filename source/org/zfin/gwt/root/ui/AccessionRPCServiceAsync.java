package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.*;

import java.util.List;

/**
 */
public interface AccessionRPCServiceAsync {

    void isValidAccession(String accessionNumber, String type, AsyncCallback<String> valid);
}

