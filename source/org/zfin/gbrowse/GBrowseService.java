package org.zfin.gbrowse;

import org.apache.log4j.Logger;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.gbrowse.repository.GBrowseRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

public class GBrowseService {

    private final static Logger logger = Logger.getLogger(GBrowseService.class);
    public static GBrowseRepository gbrowseRepository = RepositoryFactory.getGBrowseRepository();


    /**
     * Returns a Map from Contig (chromosome) to a Set of GBrowseFeature objects.
     * In a normal case, they'll all be on the same chromosome, but because the
     * data isn't that clean yet, features can be spread around and the code
     * that uses this method wants to handle it on a per chromosome basis.
     *
     * @param marker marker to get features for, only used for genes so far
     * @return A Map that maps from a Contig to a collection of gbrowse features on that Contig
     */
    public static Map<GBrowseContig, Set<GBrowseFeature>> getGBrowseFeaturesGroupedByContig(Marker marker) {
        Map<GBrowseContig, Set<GBrowseFeature>> featureMap = new TreeMap<GBrowseContig, Set<GBrowseFeature>>();

        Set<GBrowseFeature> featureSet = gbrowseRepository.getGBrowseFeaturesForMarker(marker);
        for (GBrowseFeature feature : featureSet) {
            if (!featureMap.keySet().contains(feature.getContig())) {
                featureMap.put(feature.getContig(), new TreeSet<GBrowseFeature>());
            }
            featureMap.get(feature.getContig()).add(feature);
        }
        return featureMap;
    }

    /**
     * This method is intended to produce GBrowseImage objects (basically the img & href urls)
     * showing all transcripts of a gene, with an image per chromosome (as necessary).
     *
     * @param gene                  the gene that we're creating images for
     * @param highlightedTranscript a transcript to highlight in the image(s) (optional, can be null)
     * @return A collection of GBrowseImage objects
     */
    public static List<GBrowseImage> getGBrowseTranscriptImages(Marker gene, Transcript highlightedTranscript) {
        List<GBrowseImage> images = new ArrayList<GBrowseImage>();
        Map<GBrowseContig, Set<GBrowseFeature>> featureMap = getGBrowseFeaturesGroupedByContig(gene);

        logger.debug("got GBrowse FeatureMap:" + featureMap.toString());

        for (Set<GBrowseFeature> features : featureMap.values()) {
            GBrowseImage image = buildTranscriptGBrowseImage(features, highlightedTranscript);
            if (image != null) {
                images.add(image);
                logger.debug("adding GBrowse image" + image.getImageURL());
            }
        }

        return images;
    }

    /**
     * Converts a collection of GBrowseFeatures into a GBrowseImage object.
     * <p/>
     * Images placed on the fake chromosomes AB & U will show as a link rather than
     * an image, because they're substandard data.
     *
     * @param features              GBrowseFeature objects, should all be on the same chromosome
     * @param highlightedTranscript a transcript to highlight in the image (optional, can be null)
     * @return
     */
    public static GBrowseImage buildTranscriptGBrowseImage(Set<GBrowseFeature> features,
                                                           Transcript highlightedTranscript) {
        if (features == null || features.size() == 0)
            return null;

        GBrowseImage image = new GBrowseImage();

        GBrowseFeature firstFeature = features.iterator().next();
        GBrowseContig contig = firstFeature.getContig();

        StringBuffer imageURL = new StringBuffer();
        imageURL.append("/");
        imageURL.append(ZfinProperties.getGBrowseImg());
        imageURL.append("?grid=0");
        imageURL.append("&options=mRNA 0");
        imageURL.append("&type=mRNA");

        StringBuffer linkURL = new StringBuffer();
        linkURL.append("/");
        linkURL.append(ZfinProperties.getGBrowse());

        String region = getRegion(features, contig);

        StringBuffer linkText = new StringBuffer();

        if (GBrowseContig.AB.equals(contig.getName())
                || GBrowseContig.U.equals(contig.getName())) {
            image.setLinkWithoutImage(true);
            linkText.append("<small>View gene region on ");
            linkText.append(contig);
            linkText.append(" chromosome</small>");
            if (GBrowseContig.AB.equals(contig.getName())) {
                image.setNote(GBrowseContig.AB_NOTE);
            } else {
                image.setNote(GBrowseContig.U_NOTE);
            }

        } else {
            //they're on a normal chromosome, show a normal image
            imageURL.append("&name=");
            imageURL.append(region);
            if (highlightedTranscript != null) {
                imageURL.append("&h_feat=");
                imageURL.append(highlightedTranscript.getAbbreviation());
            }
        }

        linkURL.append("?name=");
        linkURL.append(region);


        image.setLinkText(linkText.toString());
        image.setImageURL(imageURL.toString());
        image.setLinkURL(linkURL.toString());

        logger.debug(contig + " imageURL: " + imageURL.toString());
        logger.debug(contig + " linkURL: " + linkURL.toString());

        return image;
    }

    /**
     * Creates a string in the format: "1:1..10000" for a set of features,
     * To get a sensible value, they should all be on the same contig
     * <p/>
     * (otherwise it'll use the contig value of the first one and
     * the the lowest and highest values could come from another
     * chromosome)
     *
     * @param features
     * @return
     */
    public static String getRegion(Set<GBrowseFeature> features, GBrowseContig contig) {

        String start = getMinimumLocation(features).toString();
        String end = getMaximumLocation(features).toString();

        StringBuffer region = new StringBuffer();
        region.append(contig.getName());
        region.append(":");
        region.append(start);
        region.append("..");
        region.append(end);

        return region.toString();
    }

    /**
     * Returns the lowest location for a set of features, probably
     * only sensible to use if you know all of the features are on
     * the same contig
     *
     * @param features GBrowseFeature objects, should be on the same contig
     * @return lowest numbered location from the feature set
     */
    public static Long getMinimumLocation(Set<GBrowseFeature> features) {
        Long low = Long.MAX_VALUE;
        for (GBrowseFeature feature : features) {
            if (feature.getStart() < low)
                low = feature.getStart();
        }
        return low;
    }

    /**
     * Returns the highest location for a set of features, probably
     * only sensible to use if you know all of the features are on
     * the same contig
     *
     * @param features GBrowseFeature objects, should be on the same contig
     * @return highest numbered location from the feature set
     */
    public static Long getMaximumLocation(Set<GBrowseFeature> features) {
        Long high = Long.MIN_VALUE;
        for (GBrowseFeature feature : features) {
            if (feature.getEnd() > high)
                high = feature.getEnd();
        }
        return high;
    }

}


