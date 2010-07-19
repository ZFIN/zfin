package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.root.dto.FilterValuesDTO;

/**
 * RPC Async Class for the Curation module: Filter bar being shared by FX and Pheno tab.
 */
public interface CurationFilterRPCAsync {

    /**
     * Retrieve a list of all fish that are used in the experiment section.
     *
     * @param publicationID Publication
     * @param async         callback
     */
    void getPossibleFilterValues(String publicationID, AsyncCallback<FilterValuesDTO> async);

    void getFilterValues(String publicationID, AsyncCallback<FilterValuesDTO> callback);

    void setFilterType(String publicationID, String value, String type, AsyncCallback callback);

    
}