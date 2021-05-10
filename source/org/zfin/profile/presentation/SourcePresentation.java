package org.zfin.profile.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.profile.Organization;

public class SourcePresentation extends EntityPresentation {
    public static final String ORGANIZATION_URI = "profile/view/";

    public static String getLink(Organization org) {
        if (org.getLab())
            return getLabLink(org);
        else if (org.getCompany())
            return getCompanyLink(org);
        else
            return "";  //zfin jump as a punt?
    }

    public static String getLabLink(Organization org) {
        return getLink(org.getZdbID(), org.getName());
    }

    public static String getCompanyLink(Organization org) {
        return getLink(org.getZdbID(), org.getName());
    }

    public static String getLink(String organizationZdbID, String organizationName) {
        return getTomcatLink(ORGANIZATION_URI, organizationZdbID, organizationName);
    }

    public static String getUrl(Organization org) {
        return getUrl(org.getZdbID());
    }

    public static String getUrl(String organizationZdbID) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTomcatUrlStart());
        sb.append(ORGANIZATION_URI);
        sb.append(organizationZdbID);
        return sb.toString();
    }

    /**
     * Should be of the form.
     * [atp6va0a1|http://zfin.org/ZDB-GENE-030131-302|ATPase, H+ transporting, lysosomal V0 subunit a isoform 1]
     *
     * @param organization Organization to render.
     * @return A rendered wiki link.
     */
    public static String getWikiLink(Organization organization) {
        return getWikiLink("", organization.getZdbID(), organization.getName(), organization.getName());
    }
}
