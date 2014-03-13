package org.zfin.gbrowse;

import org.apache.log4j.Logger;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.properties.ZfinPropertiesEnum;


public class GBrowseService {

    private final static Logger logger = Logger.getLogger(GBrowseService.class);

    public static GBrowseImage buildTranscriptGBrowseImage(Marker gene, Transcript highlightedTranscript) {
        GBrowseImage image = new GBrowseImage();

        StringBuffer imageURL = new StringBuffer();
        imageURL.append("/");
        imageURL.append(ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT);
        imageURL.append("?grid=0");
        imageURL.append("&options=mRNA 0");
        imageURL.append("&type=mRNA");

        StringBuffer linkURL = new StringBuffer();
        linkURL.append("/");
        linkURL.append(ZfinPropertiesEnum.GBROWSE_PATH_FROM_ROOT);

        imageURL.append("&name=");
        imageURL.append(gene.getAbbreviation());
        if (highlightedTranscript != null) {
            imageURL.append("&h_feat=");
            imageURL.append(highlightedTranscript.getAbbreviation());
        }

        linkURL.append("?name=");
        linkURL.append(gene.getAbbreviation());


        image.setImageURL(imageURL.toString());
        image.setLinkURL(linkURL.toString());

        return image;
    }




}


