package org.zfin.datatransfer.go;

import org.apache.log4j.Logger;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;

/**
 */
public class PaintGafParser extends FpInferenceGafParser {

    public final static String PAINT_DEFAULT_PUB = GOREF_PREFIX + "0000033";
    private Logger logger = Logger.getLogger(PaintGafParser.class);

    @Override
    protected GafEntry parseGafEntry(String line) {
        GafEntry gafEntry = super.parseGafEntry(line);
        gafEntry.setPubmedId(PAINT_DEFAULT_PUB);
        return gafEntry;
    }

    @Override
    protected boolean isValidGafEntry(GafEntry gafEntry) {
        boolean validGafEntry = super.isValidGafEntry(gafEntry);
        if(validGafEntry){
            // we don't process ENSDARPs
            if(gafEntry.getEntryId().startsWith("ENSDARP")){
                logger.debug("we don't process ensdarps yet [" + gafEntry.getEntryId() + " throwing out: " + gafEntry);
                return false ;
            }
        }
        return validGafEntry;
    }
}
