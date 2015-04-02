package org.zfin.gbrowse.presentation;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBrowseImageSimilarComparator implements Comparator<GBrowseImage> {

    private double threshold;

    public GBrowseImageSimilarComparator(double threshold) {
        this.threshold = threshold;
    }

    public GBrowseImageSimilarComparator() {
        this(0.10);
    }

    @Override
    public int compare(GBrowseImage image1, GBrowseImage image2) {
        String landmark1 = image1.getLandmark();
        String landmark2 = image2.getLandmark();

        if (landmark1.equals(landmark2)) {
            return 0;
        }

        Pattern rangePattern = Pattern.compile("(\\d+)(?:%3A|:)(\\d+)..(\\d+)");
        Matcher match1 = rangePattern.matcher(landmark1);
        Matcher match2 = rangePattern.matcher(landmark2);

        if (match1.matches() && match2.matches()) {
            int chr1 = Integer.parseInt(match1.group(1), 10);
            int chr2 = Integer.parseInt(match2.group(1), 10);
            int start1 = Integer.parseInt(match1.group(2), 10);
            int start2 = Integer.parseInt(match2.group(2), 10);
            int end1 = Integer.parseInt(match1.group(3), 10);
            int end2 = Integer.parseInt(match2.group(3), 10);

            if (chr1 != chr2) {
                return Integer.compare(chr1, chr2);
            }

            double range1 = end1 - start1;
            double range2 = end2 - start2;
            double rangeAvg = (range1 + range2) / 2;
            double rangePercentDifference = Math.abs((range1 - range2) / rangeAvg);
            if (rangePercentDifference < this.threshold) {
                double startDifference = Math.abs(((double) (start1 - start2)) / rangeAvg);
                if (startDifference < this.threshold) {
                    return  0;
                } else {
                    return Integer.compare(start1, start2);
                }
            } else {
                return Integer.compare(start1, start2);
            }
        } else {
            return landmark1.compareTo(landmark2);
        }
    }

}
