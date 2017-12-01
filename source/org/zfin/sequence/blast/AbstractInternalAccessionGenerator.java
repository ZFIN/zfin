package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.database.InformixUtil;

public abstract class AbstractInternalAccessionGenerator {

    private static final Logger logger = Logger.getLogger(AbstractInternalAccessionGenerator.class);

    public abstract String getInternalAcessionHeader();

    public synchronized String generateAccession() {
        String accessionNumber = InformixUtil.runDBFunction("getZfinAccessionNumber", getInternalAcessionHeader());
        if (accessionNumber == null) {
            logger.error("failed to return sequence value");
        }
        return accessionNumber;
    }
}
