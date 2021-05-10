package org.zfin.infrastructure.delete;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.infrastructure.EntityZdbID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * interface defines a validate method that checks constraints on a given entity
 */
public abstract class AbstractDeleteEntityRule {

    protected EntityZdbID entity;
    protected String zdbID;

    // name of the entity and a list of entities that have a reference to the main object.
    protected List<DeleteValidationReport> validationReportList = new ArrayList<>(5);

    public void logDeleteOperation() {
        getInfrastructureRepository().insertUpdatesTable(zdbID, entity.getEntityType(), zdbID, "", entity.getEntityName() + " deleted through UI");
    }

    protected void addToValidationReport(String validationMessage) {
        DeleteValidationReport report = new DeleteValidationReport(validationMessage);
        validationReportList.add(report);

    }

    protected void addToValidationReport(String entityName, Collection<? extends EntityZdbID> collection) {
        if (CollectionUtils.isEmpty(collection))
            return;
        DeleteValidationReport report = new DeleteValidationReport(entityName, collection);
        validationReportList.add(report);
    }

    public EntityZdbID getEntity() {
        return entity;
    }

}
