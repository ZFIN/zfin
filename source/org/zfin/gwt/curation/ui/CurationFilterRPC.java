package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.zfin.gwt.root.dto.FilterValuesDTO;

/**
 * GWT class to facilitate curation filtering on FX and Pheno.
 */
public interface CurationFilterRPC extends RemoteService {

    /**
     * Retrieve a list of all fish, figures and genes that are used in the experiment section.
     *
     * @param publicationID Publication
     * @return list of fish
     */
    public FilterValuesDTO getPossibleFilterValues(String publicationID) throws PublicationNotFoundException;

    /**
     * Retrieve the fish if for the fx filter bar
     *
     * @param publicationID publication
     * @return Fish dto
     */
    FilterValuesDTO getFilterValues(String publicationID);

    /**
     * Save the filter element zdb ID
     *
     * @param publicationID publication
     * @param zdbID         zdbID
     */
    void setFilterType(String publicationID, String zdbID, String type);


    /**
     * Utility/Convenience class.
     * Use CurationExperimentRPC.App.getInstance() to access static instance of CurationExperimentRPCAsync
     */
    public static class Application {
        private static final CurationFilterRPCAsync INSTANCE;

        static {
            INSTANCE = (CurationFilterRPCAsync) GWT.create(CurationFilterRPC.class);
            ((ServiceDefTarget) INSTANCE).setServiceEntryPoint("/ajax/curation-filter");
        }

        public static CurationFilterRPCAsync getInstance() {
            return INSTANCE;
        }
    }


}