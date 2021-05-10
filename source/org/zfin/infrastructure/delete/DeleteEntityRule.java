package org.zfin.infrastructure.delete;

import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.presentation.DeleteRecordBean;
import org.zfin.publication.Publication;

import java.util.List;

/**
 * interface defines a validate method that checks constraints on a given entity
 */
public interface DeleteEntityRule {

    public List<DeleteValidationReport> validate();

    public void prepareDelete();

    public EntityZdbID getEntity();

    public Publication getPublication();

    public void logDeleteOperation();
}
