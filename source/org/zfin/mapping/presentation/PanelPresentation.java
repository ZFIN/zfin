package org.zfin.mapping.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.mapping.Panel;


public class PanelPresentation extends EntityPresentation {

    private static final String uri = "mapping/panel-detail/";

    public static String getLink(Panel panel) {
        return getTomcatLink(uri, panel.getZdbID(), panel.getName() + " ("+panel.getAbbreviation()+")", null);
    }

}
