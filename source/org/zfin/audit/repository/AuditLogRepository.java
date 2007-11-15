package org.zfin.audit.repository;

import org.zfin.audit.AuditLogItem;

import java.util.List;

/**
 * This class defines the main methods to access audit log persistence storage.
 */
public interface AuditLogRepository {

    AuditLogItem getLatestAuditLogItem(String recordID);

    List<AuditLogItem> getAuditLogItems(String recordID);


}
