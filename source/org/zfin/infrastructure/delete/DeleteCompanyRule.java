package org.zfin.infrastructure.delete;

import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Organization;
import org.zfin.publication.Publication;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getProfileRepository;

public class DeleteCompanyRule extends AbstractDeleteEntityRule implements DeleteEntityRule {

    public DeleteCompanyRule(String zdbID) {
        this.zdbID = zdbID;
    }

    @Override
    public List<DeleteValidationReport> validate() {
        Organization company = getProfileRepository().getOrganizationByZdbID(zdbID);
        addToValidationReport("Company as Source: List of Markers", company.getMarkerSourceList());
        addToValidationReport("Company as Source: List of Markers", company.getMarkerSupplierList());
        addToValidationReport("Company as Source: List of Features", company.getFeatureSourceList());
        addToValidationReport("Company as Supplier: List of Features", company.getFeatureSupplierList());
        addToValidationReport("Company as Source: List of Genotypes", company.getGenotypeSourceList());
        addToValidationReport("Company as Supplier: List of Genotypes", company.getGenotypeSupplierList());

        // member list
        if (company instanceof Lab lab) {
            addToValidationReport(lab.getName() + " has the following lab members:", lab.getMemberList());
        }
        else if (company instanceof Company company2) {
            addToValidationReport(company2.getName() + " has the following lab members:", company2.getMemberList());
        }

        entity = company;
        return validationReportList;
    }

    @Override
    public void prepareDelete() {
        entity = getProfileRepository().getOrganizationByZdbID(zdbID);
    }

    @Override
    public Publication getPublication() {
        return null;
    }
}
