/**
 *  Class Hit.
 */
package org.zfin.sequence.blast ;

import org.zfin.sequence.Accession ;
import org.zfin.marker.Marker;
import java.util.List ;
import java.util.ArrayList ;

public class Hit {
    private String zdbID ;
    private  int hitNumber ;
    private int score ;
    private double expectValue ;
    private int positivesNumerator;
    private int positivesDenominator;

    /*changed accession to targetAccession to not confuse the query and the target accession numbers*/
    private Accession targetAccession;
    private String alignment ;
//    private Marker zfinAccession ;
//    private String queryZdbId;
    private Query query;

    public static double noHitExpectValue = 1000;
    public static int noHitScore = 0;

    public String getSpecies() {
        return null ;
    }


    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID()
    {
        return zdbID;
    }

    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID)
    {
        this.zdbID = zdbID;
    }

    /**
     * Get score.
     *
     * @return score as int.
     */
    public int getScore()
    {
        return score;
    }

    /**
     * Set score.
     *
     * @param score the value to set.
     */
    public void setScore(int score)
    {
        this.score = score;
    }

    /**
     * Get expectValue.
     *
     * @return expectValue as double.
     */
    public double getExpectValue()
    {
        return expectValue;
    }

    /**
     * Set expectValue.
     *
     * @param expectValue the value to set.
     */
    public void setExpectValue(double expectValue)
    {
        this.expectValue = expectValue;
    }

    /**
     * Get positivesNumerator.
     *
     * @return positivesNumerator as int.
     */
    public int getPositivesNumerator()
    {
        return positivesNumerator;
    }

    /**
     * Set positivesNumerator.
     *
     * @param positivesNumerator the value to set.
     */
    public void setPositivesNumerator(int positivesNumerator)
    {
        this.positivesNumerator = positivesNumerator;
    }

    /**
     * Get positivesDenominator.
     *
     * @return positivesDenominator as int.
     */
    public int getPositivesDenominator()
    {
        return positivesDenominator;
    }

    /**
     * Set positivesDenominator.
     *
     * @param positivesDenominator the value to set.
     */
    public void setPositivesDenominator(int positivesDenominator)
    {
        this.positivesDenominator = positivesDenominator;
    }

    /**
     * Get targetAccession.
     *
     * @return targetAccession as Accession.
     */
    public Accession getTargetAccession()
    {
        return targetAccession;
    }

    /**
     * Set targetAccession.
     *
     * @param targetAccession the value to set.
     */
    public void setTargetAccession(Accession targetAccession)
    {
        this.targetAccession = targetAccession;
    }

    /**
     * Get alignment.
     *
     * @return alignment as String.
     */
    public String getAlignment()
    {
        return alignment;
    }

    /**
     * Set alignment.
     *
     * @param alignment the value to set.
     */
    public void setAlignment(String alignment)
    {
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
     * Get formattedAlignment.  TODO: Replace with a better jstl call using function.
     *
     * @return formattedAlignment as String.
     */
    public String getFormattedAlignment()
    {
        if(alignment!=null){
            return alignment.replace( "\\n","\n") ;
        }
        else{
            return null ;
        }
    }
    public short getPercentAlignment()
    {
      return (short)(((double)positivesNumerator / (double)positivesDenominator)*100);

    }
    
    public int getHitNumber() {
        return hitNumber;
    }
    public void setHitNumber(int hitNumber) {
            this.hitNumber = hitNumber;
        }

}


