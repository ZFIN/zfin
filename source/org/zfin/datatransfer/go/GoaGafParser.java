package org.zfin.datatransfer.go;

import org.apache.log4j.Logger;

/**
 */
public class GoaGafParser extends FpInferenceGafParser {

    private Logger logger = Logger.getLogger(GoaGafParser.class);

    protected static final String REF_GENOME_CREATED_BY = "RefGenome";

    protected boolean isValidGafEntry(GafEntry gafEntry) {
        boolean validGafEntry = super.isValidGafEntry(gafEntry);
        // exclude GOC Created BY
        if (validGafEntry) {
            if (gafEntry.getCreatedBy().equals(GOC_CREATED_BY)) {
                return false;
            }
            if (gafEntry.getCreatedBy().equals(REF_GENOME_CREATED_BY)) {
                logger.debug("Exclude PAINT reference that comes from GOA by created by: " + gafEntry.toString());
                return false;
            }
            if (gafEntry.getPubmedId().equals(PaintGafParser.PAINT_DEFAULT_PUB)) {
                logger.debug("Exclude PAINT reference that comes from GOA by Pub: " + gafEntry.toString());
                return false;
            }
        }
        return validGafEntry;
    }

}
