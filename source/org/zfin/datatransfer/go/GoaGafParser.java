package org.zfin.datatransfer.go;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class GoaGafParser extends FpInferenceGafParser {

    private Logger logger = LogManager.getLogger(GoaGafParser.class);

    protected static final String REF_GENOME_CREATED_BY = "RefGenome";

    protected boolean isValidGafEntry(GafEntry gafEntry) {
        boolean validGafEntry = super.isValidGafEntry(gafEntry);
        // exclude GOC Created BY
        if (validGafEntry) {
            if (gafEntry.getCreatedBy().equals(GOC_CREATED_BY)) {
                return false;
            }
        }
        return validGafEntry;
    }

}
