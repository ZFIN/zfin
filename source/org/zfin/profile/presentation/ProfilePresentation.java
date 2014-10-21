package org.zfin.profile.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;

public class ProfilePresentation extends EntityPresentation{

    public static String getLink(Organization organization) {
        return getViewLink(organization.getZdbID(), organization.getName(), null,"organization-link");
    }

    public static String getLinkStartTag(Organization organization) {
        if (organization == null)
            return null;
        return "<a href=\"/" + organization.getZdbID() + "\">";
    }

    public static String getLinkStartTag(Person person) {
        if (person == null)
            return null;
        return "<a href=\"/" + person.getZdbID() + "\">";
    }

    public static String getLinkEndTag() {
        return "</a>";
    }

    public static String getLinkByZfinEntity(ZfinEntity entity) {
        return "<a href=\"/" + entity.getID() + "\">" + entity.getName() + "</a>";
    }


}
