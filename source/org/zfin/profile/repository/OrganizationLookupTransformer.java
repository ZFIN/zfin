package org.zfin.profile.repository;

import org.hibernate.transform.BasicTransformerAdapter;
import org.zfin.profile.presentation.OrganizationLookupEntry;

public class OrganizationLookupTransformer extends BasicTransformerAdapter {
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        OrganizationLookupEntry organizationLookupEntry = new OrganizationLookupEntry();
        String zdbID = tuple[0].toString();
        if (zdbID.startsWith("ZDB-LAB")) {
            organizationLookupEntry.setType("Lab");
        }
        else
        if (zdbID.startsWith("ZDB-COMPANY")) {
            organizationLookupEntry.setType("Company");
        }
        else{
            organizationLookupEntry.setType("???");
        }
        organizationLookupEntry.setId(zdbID);
        organizationLookupEntry.setName(tuple[1].toString());
        return organizationLookupEntry;
    }
}
