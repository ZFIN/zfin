package org.zfin.publication.presentation;

import org.zfin.expression.Image;
import org.zfin.framework.presentation.EntityPresentation;


public class ImagePresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-imageview.apg&OID=";
    private static final String imageLoadUp = "/imageLoadUp/";

    public static String getLinkStartTag(Image image) {
        return getWebdriverStartTag(uri, image.getZdbID());
    }

    public static String getLinkEndTag() {
        return "</a>";
    }


    public static String getLink(Image image) {
        StringBuilder thumb = new StringBuilder();
        thumb.append("<img src=\"");
        thumb.append(getThumbnailUri(image));
        thumb.append("\">");

        return getWebdriverLink(uri, image.getZdbID(), thumb.toString());
    }

    public static String getThumbnailUri(Image image) {
        return imageLoadUp + image.getThumbnail();
    }

}
