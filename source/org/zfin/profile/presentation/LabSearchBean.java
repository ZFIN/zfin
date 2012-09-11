package org.zfin.profile.presentation;

import org.zfin.gwt.root.util.StringUtils;
import org.zfin.util.URLCreator;

/**
 */
public class LabSearchBean extends AbstractProfileSearchBean{

    protected URLCreator creator = new URLCreator("/action/profile/lab/search/execute");

    @Override
    public String getActionUrl() {
        if (StringUtils.isNotEmpty(name)) {
            creator.addNamevaluePair("name", name);
        } else {
            creator.removeNamevaluePair("name");
        }
        if (StringUtils.isNotEmpty(address)) {
            creator.addNamevaluePair("address", address);
        } else {
            creator.removeNamevaluePair("address");
        }
        if (StringUtils.isNotEmpty(contains)) {
            creator.addNamevaluePair("contains", contains);
        } else {
            creator.removeNamevaluePair("contains");
        }
        if (StringUtils.isNotEmpty(containsType)) {
            creator.addNamevaluePair("containsType", containsType);
        } else {
            creator.removeNamevaluePair("containsType");
        }
        if (StringUtils.isNotEmpty(view)) {
            creator.addNamevaluePair("view", view);
        } else {
            creator.removeNamevaluePair("view");
        }
        creator.addNamevaluePair("maxDisplayRecords", String.valueOf(getMaxDisplayRecords()));
        return creator.getFullURLPlusSeparator();
    }
}
