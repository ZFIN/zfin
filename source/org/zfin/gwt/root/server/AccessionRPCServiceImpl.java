package org.zfin.gwt.root.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;
import org.zfin.feature.repository.FeatureService;
import org.zfin.gwt.root.ui.AccessionRPCService;
import org.zfin.sequence.ReferenceDatabase;

public class AccessionRPCServiceImpl extends RemoteServiceServlet implements AccessionRPCService {

    private transient Logger logger = Logger.getLogger(AccessionRPCServiceImpl.class);

    @Override
    public String isValidAccession(String accessionNumber, String type) {
        if (type.equals("DNA")) {
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailDna(accessionNumber);
            return referenceDatabase != null ? referenceDatabase.getForeignDB().getDbName().toString() : null;
        } else {
            ReferenceDatabase referenceDatabase = FeatureService.getForeignDbMutationDetailProtein(accessionNumber);
            return referenceDatabase != null ? referenceDatabase.getForeignDB().getDbName().toString() : null;
        }
    }
}
