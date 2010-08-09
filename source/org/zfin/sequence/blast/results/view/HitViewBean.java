package org.zfin.sequence.blast.results.view;

import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.marker.Marker;
import org.zfin.sequence.Accession;
import org.zfin.sequence.DBLink;

import java.util.List;
import java.util.Set;

/**
 * This is the view for a hit used in the blast results page.
 */
public class HitViewBean extends ExpressionMapBean{


    private String accessionNumber;
    private String id; // unformatted accession string, no not really key
    private int version;
    private DBLink hitDBLink;
    private Accession zfinAccession; // if no DBLink is found
    private Marker hitMarker ; // this is usuae ly a clone or transcript, comes through marker relation
    private boolean isMarkerIsHit = false ;
    private boolean withdrawn = false ;
    private int hitNumber;
    private int hitLength ;
    private int score ;
    private double eValue;
    private int nValue ;
    private Set<Marker> genes ;
    private String definition ;
    private List<HighScoringPair> highScoringPairs ;
    private List<GBrowseImage> gbrowseImages;


    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getHitLength() {
        return hitLength;
    }

    public void setHitLength(int hitLength) {
        this.hitLength = hitLength;
    }

    public Accession getZfinAccession() {
        return zfinAccession;
    }

    public void setZfinAccession(Accession zfinAccession) {
        this.zfinAccession = zfinAccession;
    }

    public DBLink getHitDBLink() {
        return hitDBLink;
    }

    public void setHitDBLink(DBLink hitDBLink) {
        this.hitDBLink = hitDBLink;
    }

    public boolean isMarkerIsHit() {
        return isMarkerIsHit;
    }

    public void setMarkerIsHit(boolean markerIsHit) {
        isMarkerIsHit = markerIsHit;
    }

    public Marker getHitMarker() {
        return hitMarker;
    }

    public void setHitMarker(Marker hitMarker) {
        this.hitMarker = hitMarker;
    }

    public int getHitNumber() {
        return hitNumber;
    }

    public void setHitNumber(int hitNumber) {
        this.hitNumber = hitNumber;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public double getEValue() {
        return eValue;
    }

    public void setEValue(double eValue) {
        this.eValue = eValue;
    }

    public int getNValue() {
        return nValue;
    }

    public void setNValue(int nValue) {
        this.nValue = nValue;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Marker getGene(){
        return genes.iterator().next() ;
    }

    public Set<Marker> getGenes() {
        return genes;
    }

    public void setGenes(Set<Marker> genes) {
        this.genes = genes;
    }

    public List<HighScoringPair> getHighScoringPairs() {
        return highScoringPairs;
    }

    public void setHighScoringPairs(List<HighScoringPair> highScoringPairs) {
        this.highScoringPairs = highScoringPairs;
    }


    public List<GBrowseImage> getGbrowseImages() {
        return gbrowseImages;
    }

    public void setGbrowseImages(List<GBrowseImage> gbrowseImages) {
        this.gbrowseImages = gbrowseImages;
    }


    public boolean isWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(boolean withdrawn) {
        this.withdrawn = withdrawn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HitViewBean that = (HitViewBean) o;


        // if the accessions are not null and match then return true automatically
        if(accessionNumber !=null && that.accessionNumber !=null){
            if (true== accessionNumber.equals(that.accessionNumber) ){
                return true ;
            }
            else{
                return hashCode()==that.hashCode() ;
            }
        }

        return false ;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = accessionNumber != null ? accessionNumber.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (hitDBLink != null ? hitDBLink.hashCode() : 0);
        result = 31 * result + (zfinAccession != null ? zfinAccession.hashCode() : 0);
        result = 31 * result + (hitMarker != null ? hitMarker.hashCode() : 0);
        result = 31 * result + hitNumber;
        result = 31 * result + hitLength;
        result = 31 * result + score;
        temp = eValue != +0.0d ? Double.doubleToLongBits(eValue) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + nValue;
        result = 31 * result + (genes != null ? genes.hashCode() : 0);
        result = 31 * result + (definition != null ? definition.hashCode() : 0);
        result = 31 * result + (hasExpression ? 1 : 0);
        result = 31 * result + (hasExpressionImages ? 1 : 0);
        result = 31 * result + (hasGO ? 1 : 0);
        result = 31 * result + (hasPhenotype ? 1 : 0);
        result = 31 * result + (hasPhenotypeImages ? 1 : 0);
        result = 31 * result + (highScoringPairs != null ? highScoringPairs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HitViewBean{" +
                "hitAccessionID='" + accessionNumber + '\'' +
                ", version=" + version +
                ", hitDBLink=" + hitDBLink +
                ", hitAccession=" + zfinAccession +
                ", hitMarker=" + hitMarker +
                ", isMarkerIsHit=" + isMarkerIsHit +
                ", hitNumber=" + hitNumber +
                ", hitLength=" + hitLength +
                ", score=" + score +
                ", eValue=" + eValue +
                ", nValue=" + nValue +
                ", genes=" + genes +
                ", definition='" + definition + '\'' +
                ", withdrawn='" + withdrawn+ '\'' +
                ", highScoringPairs=" + highScoringPairs +
                '}';
    }
}
