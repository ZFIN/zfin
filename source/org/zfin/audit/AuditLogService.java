package org.zfin.audit;

import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Service for audit log related business.
 */
public class AuditLogService {

    public static AuditLogItem getLatestUpdate(String zdbID){
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        AuditLogItem latestLogItem = alr.getLatestAuditLogItem(zdbID);
        return latestLogItem;
    }


}
