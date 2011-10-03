/**
 *  Class Hit.
 */
package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.Accession;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hit {
    private String zdbID;
    private int hitNumber;
    private int score;
    private double expectValue;
    private int positivesNumerator;
    private int positivesDenominator;

    /*changed accession to targetAccession to not confuse the query and the target accession numbers*/
    private Accession targetAccession;
    private String alignment = null;
    //    private Marker zfinAccession ;
//    private String queryZdbId;
    private Query query;

    public static final double noHitExpectValue = 1000;
    public static final int noHitScore = 0;

    // second group matches end of line or end of string.  Does not like [], but would be more correct.
    private final Pattern endPattern = Pattern.compile("\\p{Space}([0-9]+?)(\n|$)");
    private final Pattern startPattern = Pattern.compile("([0-9]+?)\\p{Space}");

    final Pattern descriptionPattern = Pattern.compile("(Score.*\n.*Identities.*(\n|$))");

    private final Logger logger = Logger.getLogger(Hit.class);


    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID() {
        return zdbID;
    }

    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    /**
     * Get score.
     *
     * @return score as int.
     */
    public int getScore() {
        return score;
    }

    /**
     * Set score.
     *
     * @param score the value to set.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Get expectValue.
     *
     * @return expectValue as double.
     */
    public double getExpectValue() {
        return expectValue;
    }

    /**
     * Set expectValue.
     *
     * @param expectValue the value to set.
     */
    public void setExpectValue(double expectValue) {
        this.expectValue = expectValue;
    }

    /**
     * Get positivesNumerator.
     *
     * @return positivesNumerator as int.
     */
    public int getPositivesNumerator() {
        return positivesNumerator;
    }

    /**
     * Set positivesNumerator.
     *
     * @param positivesNumerator the value to set.
     */
    public void setPositivesNumerator(int positivesNumerator) {
        this.positivesNumerator = positivesNumerator;
    }

    /**
     * Get positivesDenominator.
     *
     * @return positivesDenominator as int.
     */
    public int getPositivesDenominator() {
        return positivesDenominator;
    }

    /**
     * Set positivesDenominator.
     *
     * @param positivesDenominator the value to set.
     */
    public void setPositivesDenominator(int positivesDenominator) {
        this.positivesDenominator = positivesDenominator;
    }

    /**
     * Get targetAccession.
     *
     * @return targetAccession as Accession.
     */
    public Accession getTargetAccession() {
        return targetAccession;
    }

    /**
     * Set targetAccession.
     *
     * @param targetAccession the value to set.
     */
    public void setTargetAccession(Accession targetAccession) {
        this.targetAccession = targetAccession;
    }

    /**
     * Get alignment.
     *
     * @return alignment as String.
     */
    public String getAlignment() {
        return alignment;
    }

    /**
     * Set alignment.
     *
     * @param alignment the value to set.
     */
    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    //    /**
//     * Get zfinAccession.
//     *
//     * @return zfinAccession as DBLink.
//     */
//    public Marker getZfinAccession()
//    {
//        return zfinAccession;
//    }
//
//    /**
//     * Set zfinAccession.
//     *
//     * @param zfinAccession the value to set.
//     */
//    public void setZfinAccession(Marker zfinAccession)
//    {
//        this.zfinAccession = zfinAccession;
//    }
//
    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Get formattedAlignment.
     *
     * @return formattedAlignment as String.
     *
     * TODO: in general, the view level stuff should go further out towards the jsp
     */
    public String getFormattedAlignment() {
        if (alignment != null) {
            alignment = alignment.replace("\\n", "\n");
            String[] alignments = getHsps(alignment);
            List<String> descriptions = null;
            if (alignments.length > 1) {
                descriptions = getDescriptions(alignment);
            }
            String formattedAlignment = "";
            int alignmentNumber = 0;
            for (String alignmentRow : alignments) {
                String alignmentClass ;

                if (alignmentNumber > 0) {
                    formattedAlignment += "\n\n\n<pre>\n";
                    formattedAlignment += descriptions.get(alignmentNumber - 1);
                    formattedAlignment += "\n</pre>\n";
                }

                if (isReversed(alignmentRow)) {
                    alignmentClass  = "reno-reversed-strand";
                } else {
                    alignmentClass  = "reno-same-strand";
                }
                formattedAlignment += "\n<pre class='" + alignmentClass  + "'>\n";
                formattedAlignment += alignmentRow;
                formattedAlignment += "\n</pre>\n";

                ++alignmentNumber;
            }
            return formattedAlignment;
        } else {
            return null;
        }
    }

    public List getDescriptions(String s) {
        Matcher m = descriptionPattern.matcher(s);
        List<String> returnStrings = new ArrayList<String>() ;

        while (m.find()) {
            returnStrings.add(m.group());
        }

        return returnStrings;
    }

    public String[] getHsps(String s) {
        return s.split("Score.*\n.*\n\n");
    }

    public short getPercentAlignment() {
        return (short) (((double) positivesNumerator / (double) positivesDenominator) * 100);

    }

    public int getHitNumber() {
        return hitNumber;
    }

    public void setHitNumber(int hitNumber) {
        this.hitNumber = hitNumber;
    }

    public int getQueryStart(String s) {
        if (s == null) {
            return -1;
        }
        Matcher m = startPattern.matcher(s);
        if (m.find()) {
            return Integer.parseInt(m.group(0).trim());
        }

        logger.error("getQueryStart - Error parsing alignment string: " + s);
        return -1;
    }

    public int getQueryEnd(String s) {
//        logger.error("alignment: "+alignment);
        if (s == null) {
            return -1;
        }
        Matcher m = endPattern.matcher(s);
        if (m.find()) {
            return Integer.parseInt(m.group(0).trim());
        }
        logger.error("getQueryEnd - Error parsing alignment string: " + s);
        return -1;
    }

    public int getSubjectStart(String s) {
        if (s == null) {
            return -1;
        }
        Matcher m = startPattern.matcher(s);
        if (m.find() && m.find() && m.find()) {
            return Integer.parseInt(m.group(0).trim());
        }
        logger.error("getSubjectStart - Error parsing alignment string: " + s);
        return -1;
    }

    public int getSubjectEnd(String s) {
        if (s == null) {
            return -1;
        }
        Matcher m = endPattern.matcher(s);
        if (m.find() && m.find()) {
            return Integer.parseInt(m.group(0).trim());
        }
        logger.error("getSubjectEnd - Error parsing alignment string: " + s);
        return -1;
    }

    /**
     * "Query:  1826 TCTTAATGTAATTTATTAGGTACGTTTTCATAAGAGAAAAATATTTATGTGTCCCACAAA 1767\n" +
     * "             |||||||||||||||| |||||||||||||||||| ||||||||||||||||||||||||\n" +
     * "Sbjct:     1 TCTTAATGTAATTTATAAGGTACGTTTTCATAAGAAAAAAATATTTATGTGTCCCACAAA 60"
     *
     * @return
     */
    public boolean isReversed() {

        if (alignment == null) {
            return false;
        } else {
            for (String hsp : getHsps(alignment)) {
                if (isReversed(hsp)) {
                    return true;
                }
            }
        }

        return false;
    }


    public boolean isReversed(String s) {
        if (s == null) {
            return false;
        }
        int queryOrder = getQueryEnd(s) - getQueryStart(s);
        int subjectOrder = getSubjectEnd(s) - getSubjectStart(s);

        // if one is negative then it is a reversed order
        return (queryOrder * subjectOrder) < 0;
    }
}


