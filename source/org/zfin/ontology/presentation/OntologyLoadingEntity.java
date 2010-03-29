package org.zfin.ontology.presentation;

import org.zfin.ontology.Ontology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class OntologyLoadingEntity implements Serializable{

    private Ontology ontology;
    private LoadingData lastLoad;
    private List<LoadingData> allLoadingEvents = new ArrayList<LoadingData>(20);

    public OntologyLoadingEntity(Ontology ontology) {
        this.ontology = ontology;
    }

    /**
     * Only keep the last 10 records.
     *
     * @param dateOfLoad  date of the load
     * @param loadingTime time it took to load the ontology
     * @param numOfTerms  number of terms in the ontology
     */
    public void addLoadingEvent(Date dateOfLoad, long loadingTime, int numOfTerms) {
        allLoadingEvents.add(lastLoad);
        lastLoad = new LoadingData(dateOfLoad, loadingTime, numOfTerms);
        if (allLoadingEvents.size() > 10)
            allLoadingEvents.remove(0);
    }

    public Ontology getOntology() {
        return ontology;
    }

    public int getNumberOfTerms() {
        return lastLoad.getNumberOfTerms();
    }

    public Date getDateLastLoaded(){
        return lastLoad.getDateLastLoaded();
    }

    public double getTimeLastLoaded(){
        return (double) lastLoad.getLoadingTime()/ 1000.0;
    }

    public LoadingData getLastLoad() {
        return lastLoad;
    }

    public List<LoadingData> getAllLoadingEvents() {
        return allLoadingEvents;
    }

    private class LoadingData implements Serializable{

        private Date dateLastLoaded;
        private long loadingTime;
        private int numberOfTerms;

        private LoadingData(Date dateLastLoaded, long loadingTime, int numberOfTerms) {
            this.dateLastLoaded = dateLastLoaded;
            this.loadingTime = loadingTime;
            this.numberOfTerms = numberOfTerms;
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
    }

}
