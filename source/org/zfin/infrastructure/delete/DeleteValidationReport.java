package org.zfin.infrastructure.delete;

import org.zfin.infrastructure.EntityZdbID;

import java.util.Collection;

/**
 * Validation report for delete rules.
 */
public class DeleteValidationReport {

    private Collection<? extends EntityZdbID> entityCollection;
    private String validationMessage;

    public DeleteValidationReport(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public DeleteValidationReport(String validationMessage, Collection<? extends EntityZdbID> entityCollection) {
        this.entityCollection = entityCollection;
        this.validationMessage = validationMessage;
    }

    public Collection<? extends EntityZdbID> getEntityCollection() {
        return entityCollection;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public String getEntityType() {
        if (entityCollection == null)
            return null;
        return entityCollection.iterator().next().getEntityType();
    }
}
