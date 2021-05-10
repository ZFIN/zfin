package org.zfin.infrastructure.delete;

import org.zfin.feature.FeaturePrefix;
import org.zfin.profile.Lab;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

public class DeleteLabRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteLabRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Lab lab = (Lab) RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbID);
        String baseMessageSource = lab.getName() + " is the source of the following";
        String baseMessageSupplier = lab.getName() + " is the supplier of the following";
        addToValidationReport(baseMessageSource + " Markers", lab.getMarkerSourceList());
        addToValidationReport(baseMessageSupplier + " Markers", lab.getMarkerSupplierList());
        addToValidationReport(baseMessageSource + " Features", lab.getFeatureSourceList());
        addToValidationReport(baseMessageSupplier + " Features", lab.getFeatureSupplierList());
        addToValidationReport(baseMessageSource + " Genotypes", lab.getGenotypeSourceList());
        addToValidationReport(baseMessageSupplier + " Genotypes", lab.getGenotypeSupplierList());
        // lab members
        addToValidationReport(lab.getName() + " has the following lab members:", lab.getMemberList());
        entity = lab;

        List<FeaturePrefix> designations = RepositoryFactory.getFeatureRepository().getCurrentLabPrefixesById(zdbID, false);
        addToValidationReport(lab.getName() + " has the following lab designations", designations);
        return validationReportList;

    }

    @Override
    public void prepareDelete() {
        entity = RepositoryFactory.getProfileRepository().getOrganizationByZdbID(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
