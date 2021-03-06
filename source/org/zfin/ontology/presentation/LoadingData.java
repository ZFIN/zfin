package org.zfin.ontology.presentation;

import java.io.Serializable;
import java.util.Date;

/**
 * A single loading event.
 */
public class LoadingData implements Serializable {

    private Date dateLastLoaded;
    private long loadingTime;
    private int numberOfTerms;
    private int numberOfObsoletedTerms;
    private int numberOfAliases;
    private int numberOfKeys;
    private int numberOfValues;

    public LoadingData(Date dateLastLoaded, long loadingTime, int numOfTerms, int numOfObsoletedTerms, int numOfAliases,int numOfKeys,int numOfValues) {
        super();
        this.dateLastLoaded = dateLastLoaded;
        this.loadingTime = loadingTime;
        this.numberOfTerms = numOfTerms;
        this.numberOfObsoletedTerms = numOfObsoletedTerms;
        this.numberOfAliases = numOfAliases;
        this.numberOfKeys = numOfKeys ;
        this.numberOfValues = numOfValues ;
    }

    public Date getDateLastLoaded() {
        return dateLastLoaded;
    }

    public long getLoadingTime() {
        return loadingTime;
    }

    public int getNumberOfTerms() {
        return numberOfTerms;
    }

    public int getNumberOfObsoletedTerms() {
        return numberOfObsoletedTerms;
    }

    public int getTotalNumberOfTerms() {
        return numberOfTerms + numberOfObsoletedTerms;
    }

    public int getNumberOfAliases() {
        return numberOfAliases;
    }

    public void setNumberOfAliases(int numberOfAliases) {
        this.numberOfAliases = numberOfAliases;
    }

    public int getNumberOfKeys() {
        return numberOfKeys;
    }

    public int getNumberOfValues() {
        return numberOfValues;
    }
}
