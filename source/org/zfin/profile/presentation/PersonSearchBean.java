package org.zfin.profile.presentation;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.util.URLCreator;

/**
 */
public class PersonSearchBean extends AbstractProfileSearchBean{

    protected URLCreator creator = new URLCreator("/action/profile/person/search/execute");

    @Override
    public String getActionUrl() {
        if (StringUtils.isNotEmpty(name)) {
            creator.addNameValuePair("name", name);
        } else {
            creator.removeNameValuePair("name");
        }
        if (StringUtils.isNotEmpty(address)) {
            creator.addNameValuePair("address", address);
        } else {
            creator.removeNameValuePair("address");
        }
        if (StringUtils.isNotEmpty(contains)) {
            creator.addNameValuePair("contains", contains);
        } else {
            creator.removeNameValuePair("contains");
        }
        if (StringUtils.isNotEmpty(containsType)) {
            creator.addNameValuePair("containsType", containsType);
        } else {
            creator.removeNameValuePair("containsType");
        }
        if (StringUtils.isNotEmpty(view)) {
            creator.addNameValuePair("view", view);
        } else {
            creator.removeNameValuePair("view");
        }
        creator.addNameValuePair("maxDisplayRecords", String.valueOf(getMaxDisplayRecords()));
        return creator.getFullURLPlusSeparator();
    }
}
