package org.zfin.publication.presentation;

import org.zfin.expression.Image;
import org.zfin.framework.presentation.EntityPresentation;


public class ImagePresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-imageview.apg&OID=";
    private static final String imageLoadUp = "/imageLoadUp/";
   // public static final String IMAGE_URI = "publication/image/";
   public static final String IMAGE_URI = "image/view/";
    public static String getLinkStartTag(Image image) {
      //return getWebdriverStartTag(uri, image.getZdbID());
        return getViewStartTag(image.getZdbID());

    }

    public static String getLinkEndTag() {
        return "</a>";
    }


    public static String getLink(Image image) {
        /*if (image == null)
            return null;
        StringBuilder thumb = new StringBuilder();
        thumb.append("<img src=\"");
        thumb.append(getThumbnailUri(image));
        thumb.append("\">");

        return getWebdriverLink(uri, image.getZdbID(), thumb.toString());
        String link = getViewLink(image.getZdbID(), "", "", null);
        return link;*/
        if (image == null)
            return null;
        StringBuilder thumb = new StringBuilder();
        thumb.append("<img src=\"");
        thumb.append(getThumbnailUri(image));
        thumb.append("\">");

       // return getWebdriverLink(uri, image.getZdbID(), thumb.toString());
        return getTomcatLink(IMAGE_URI, image.getZdbID(), thumb.toString());
    }

    public static String getThumbnailUri(Image image) {
        if (image == null)
            return null;
        return imageLoadUp + image.getThumbnail();
    }

}
