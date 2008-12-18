package org.zfin.publication.presentation;

import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.expression.Image;
import org.zfin.properties.ZfinProperties;


public class ImagePresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-imageview.apg&OID=";

    public static String getLinkStartTag(Image image) {
        return getWebdriverStartTag(uri, image.getZdbID());
    }

    public static String getLinkEndTag() { return "</a>"; }

}
