/**
 *  Class Hit.
 */
package org.zfin.sequence.blast;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.sequence.Accession;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
@Entity
@Table(name = "blast_hit")
public class Hit {

    @Id
    @GeneratedValue(generator = "zdbIdGeneratorForHit")
    @org.hibernate.annotations.GenericGenerator(name = "zdbIdGeneratorForHit", strategy = "org.zfin.database.ZdbIdGenerator", parameters = {
            @org.hibernate.annotations.Parameter(name = "type", value = "BHIT")
    })
    @Column(name = "bhit_zdb_id", nullable = false)
    private String zdbID;

    @Column(name = "bhit_hit_number", nullable = false)
    private int hitNumber;

    @Column(name = "bhit_score", nullable = false)
    private int score;

    @Column(name = "bhit_expect_value", nullable = false)
    private double expectValue;

    @Column(name = "bhit_positives_numerator", nullable = false)
    private int positivesNumerator;

    @Column(name = "bhit_positives_denominator", nullable = false)
    private int positivesDenominator;

    /*changed accession to targetAccession to not confuse the query and the target accession numbers*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bhit_target_accbk_pk_id", nullable = false)
    private Accession targetAccession;

    @Column(name = "bhit_alignment")
    private String alignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bhit_bqry_zdb_id", nullable = false)
    private Query query;

    public static final double noHitExpectValue = 1000;
    public static final int noHitScore = 0;

    // second group matches end of line or end of string.  Does not like [], but would be more correct.
    @Transient
    private final Pattern endPattern = Pattern.compile("\\p{Space}([0-9]+?)(\n|$)");
    @Transient
    private final Pattern startPattern = Pattern.compile("([0-9]+?)\\p{Space}");
    @Transient
    final Pattern descriptionPattern = Pattern.compile("(Score.*\n.*Identities.*(\n|$))");
    @Transient
    private final Logger logger = LogManager.getLogger(Hit.class);



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


