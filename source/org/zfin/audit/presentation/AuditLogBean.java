package org.zfin.audit.presentation;

import org.zfin.audit.AuditLogItem;

import java.util.List;

/**
 *
 */
public class AuditLogBean {

    private String zdbID;
    private List<AuditLogItem> items;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public List<AuditLogItem> getItems() {
        return items;
    }

    public void setItems(List<AuditLogItem> items) {
        this.items = items;
    }
}
