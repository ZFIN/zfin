package org.zfin.people.presentation;

import org.zfin.people.Company;
import org.zfin.people.Lab;
import org.zfin.people.Organization;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.properties.ZfinProperties;

public class SourcePresentation extends EntityPresentation {
    public static String lab_uri = "?MIval=aa-labview.apg&OID=";
    public static String company_uri = "?MIval=aa-companyview.apg&OID=";

    public static String getLink(Organization org) {
        if (org.getLab())
          return getLabLink(org);
        else if (org.getCompany())
          return getCompanyLink(org);
        else
          return "";  //zfin jump as a punt?
    }

    public static String getLabLink(Organization org) {
        return getWebdriverLink(lab_uri, org.getZdbID(), org.getName());
    }

    public static String getCompanyLink(Organization org) {
        return getWebdriverLink(company_uri, org.getZdbID(), org.getName());
    }

    /**
     * Should be of the form.
     * [atp6va0a1|http://zfin.org/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     * @param organization Organization to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Organization organization){
        return getWikiLink(ZfinProperties.getWebDriver()+ (organization.getLab()?lab_uri:company_uri)
                ,organization.getZdbID(),organization.getName(),organization.getName()) ;
    }
}
